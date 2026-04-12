package com.ravaroj.habitcurrency

import android.app.Application
import com.ravaroj.habitcurrency.data.AppContainer

class HabitCurrencyApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
