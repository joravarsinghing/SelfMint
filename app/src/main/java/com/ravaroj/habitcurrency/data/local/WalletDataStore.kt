package com.ravaroj.habitcurrency.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "wallet_prefs")

class WalletDataStore(private val context: Context) {

    companion object {
        private val BALANCE = intPreferencesKey("balance")
    }

    val balanceFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BALANCE] ?: 0
    }

    suspend fun add(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[BALANCE] ?: 0
            prefs[BALANCE] = current + amount
        }
    }

    suspend fun subtract(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[BALANCE] ?: 0
            prefs[BALANCE] = (current - amount).coerceAtLeast(0)
        }
    }

    suspend fun setBalance(amount: Int) {
        context.dataStore.edit { prefs ->
            prefs[BALANCE] = amount
        }
    }
}