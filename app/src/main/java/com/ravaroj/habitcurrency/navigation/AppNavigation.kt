package com.ravaroj.habitcurrency.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import com.ravaroj.habitcurrency.ui.theme.DashboardWhite
import com.ravaroj.habitcurrency.ui.theme.InactiveNavGrey
import com.ravaroj.habitcurrency.ui.theme.RewardsOrange
import com.ravaroj.habitcurrency.ui.theme.TasksBlue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ravaroj.habitcurrency.ui.screens.DashboardScreen
import com.ravaroj.habitcurrency.ui.screens.RewardsScreen
import com.ravaroj.habitcurrency.ui.screens.TasksScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val navItems = listOf(
        BottomNavItem.Tasks,
        BottomNavItem.Rewards,
        BottomNavItem.Dashboard
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route

                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val activeColor = when (item) {
                        BottomNavItem.Tasks -> TasksBlue
                        BottomNavItem.Rewards -> RewardsOrange
                        BottomNavItem.Dashboard -> DashboardWhite
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Text(
                                text = item.label.take(1),
                                color = if (isSelected) activeColor else InactiveNavGrey
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) activeColor else InactiveNavGrey
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            unselectedIconColor = InactiveNavGrey,
                            unselectedTextColor = InactiveNavGrey,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Tasks.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Tasks.route) {
                TasksScreen()
            }
            composable(BottomNavItem.Rewards.route) {
                RewardsScreen()
            }
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen()
            }
        }
    }
}
