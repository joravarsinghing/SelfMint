package com.ravaroj.habitcurrency.ui.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ravaroj.habitcurrency.data.local.WalletDataStore
import com.ravaroj.habitcurrency.data.repository.RewardRepository

class RewardsViewModelFactory(
    private val rewardRepository: RewardRepository,
    private val walletDataStore: WalletDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RewardsViewModel::class.java)) {
            return RewardsViewModel(rewardRepository, walletDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
