package com.ravaroj.habitcurrency.ui.dashboard

data class DashboardUiState(
    val walletBalance: Int = 0,
    val earnedToday: Int = 0,
    val spentToday: Int = 0,
    val completedToday: Int = 0,
    val activeToday: Int = 0,
    val last7DaysEarned: List<Pair<String, Int>> = emptyList(),
    val last7DaysSpent: List<Pair<String, Int>> = emptyList(),
    val walletHistory: List<Int> = emptyList(),
    val labels: List<String> = emptyList(),
    val isLoading: Boolean = true
)
