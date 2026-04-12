package com.ravaroj.habitcurrency.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

class AppSettingsDataStore(private val context: Context) {

    companion object {
        private val LAST_PROCESSED_DATE = stringPreferencesKey("last_processed_date")
    }

    val lastProcessedDateFlow: Flow<String?> = context.appSettingsDataStore.data.map { prefs ->
        prefs[LAST_PROCESSED_DATE]
    }

    suspend fun setLastProcessedDate(date: String) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[LAST_PROCESSED_DATE] = date
        }
    }

    suspend fun clearLastProcessedDate() {
        context.appSettingsDataStore.edit { prefs ->
            prefs.remove(LAST_PROCESSED_DATE)
        }
    }
}
