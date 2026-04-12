package com.ravaroj.habitcurrency.navigation

sealed class BottomNavItem(
    val route: String,
    val label: String
) {
    data object Tasks : BottomNavItem("tasks", "Tasks")
    data object Rewards : BottomNavItem("rewards", "Rewards")
    data object Dashboard : BottomNavItem("dashboard", "Dashboard")
}
