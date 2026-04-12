package com.ravaroj.habitcurrency

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.ravaroj.habitcurrency.data.repository.RolloverRepository
import com.ravaroj.habitcurrency.navigation.AppNavigation
import com.ravaroj.habitcurrency.ui.theme.HabitCurrencyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()

        val app = application as HabitCurrencyApplication
        val appContainer = app.appContainer

        lifecycleScope.launch {
            RolloverRepository(
                taskInstanceDao = appContainer.taskInstanceDao,
                dailyTaskTemplateDao = appContainer.dailyTaskTemplateDao,
                redemptionDao = appContainer.redemptionDao,
                appSettingsDataStore = appContainer.appSettingsDataStore
            ).processNewDayIfNeeded()
        }

        setContent {
            HabitCurrencyTheme {
                AppNavigation()
            }
        }
    }
}
