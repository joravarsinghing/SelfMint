package com.ravaroj.habitcurrency.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravaroj.habitcurrency.data.local.WalletDataStore
import com.ravaroj.habitcurrency.data.repository.DashboardRepository
import com.ravaroj.habitcurrency.data.repository.DemoDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val demoDataRepository: DemoDataRepository,
    private val walletDataStore: WalletDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeWallet()
        loadStats()
    }

    private fun observeWallet() {
        viewModelScope.launch {
            walletDataStore.balanceFlow.collect { balance ->
                _uiState.update { it.copy(walletBalance = balance) }
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val todayStats = dashboardRepository.getTodayStats()
            val earned7 = dashboardRepository.getLast7DaysEarned() // newest to oldest
            val spent7 = dashboardRepository.getLast7DaysSpent()   // newest to oldest

            val earnedValues = earned7.map { it.second }.reversed()
            val spentValues = spent7.map { it.second }.reversed()
            val dateLabels = earned7.map { it.first }.reversed()

            // Calculate wallet history (oldest to newest)
            // Current balance is at the end of the 7-day period (index 6)
            val currentBalance = _uiState.value.walletBalance
            val history = mutableListOf<Int>()
            
            // To get the starting balance (7 days ago), we work backwards from current
            // but it's easier to just calculate forward if we know the start.
            // Start balance = Current - Sum(Earned) + Sum(Spent)
            var runningBalance = currentBalance - earnedValues.sum() + spentValues.sum()
            
            for (i in 0..6) {
                runningBalance += (earnedValues[i] - spentValues[i])
                history.add(runningBalance)
            }

            _uiState.update {
                it.copy(
                    walletBalance = it.walletBalance,
                    earnedToday = todayStats.earnedToday,
                    spentToday = todayStats.spentToday,
                    completedToday = todayStats.completedToday,
                    activeToday = todayStats.activeToday,
                    last7DaysEarned = earned7,
                    last7DaysSpent = spent7,
                    walletHistory = history,
                    labels = dateLabels,
                    isLoading = false
                )
            }
        }
    }

    fun generateDemoData() {
        viewModelScope.launch {
            demoDataRepository.generateDemoData()
            loadStats()
        }
    }

    fun clearDemoData() {
        viewModelScope.launch {
            demoDataRepository.clearDemoData()
            loadStats()
        }
    }
}
