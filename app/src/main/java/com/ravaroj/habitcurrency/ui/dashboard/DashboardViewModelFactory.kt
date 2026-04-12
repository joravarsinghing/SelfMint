package com.ravaroj.habitcurrency.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ravaroj.habitcurrency.data.local.WalletDataStore
import com.ravaroj.habitcurrency.data.repository.DashboardRepository
import com.ravaroj.habitcurrency.data.repository.DemoDataRepository

class DashboardViewModelFactory(
    private val dashboardRepository: DashboardRepository,
    private val demoDataRepository: DemoDataRepository,
    private val walletDataStore: WalletDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(dashboardRepository, demoDataRepository, walletDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
