package com.ravaroj.habitcurrency.data

import android.content.Context
import com.ravaroj.habitcurrency.data.local.AppSettingsDataStore
import com.ravaroj.habitcurrency.data.local.AppDatabase
import com.ravaroj.habitcurrency.data.local.WalletDataStore
import com.ravaroj.habitcurrency.data.local.dao.DailyTaskTemplateDao
import com.ravaroj.habitcurrency.data.local.dao.RedemptionDao
import com.ravaroj.habitcurrency.data.local.dao.RewardDao
import com.ravaroj.habitcurrency.data.local.dao.TagDao
import com.ravaroj.habitcurrency.data.local.dao.TaskInstanceDao

class AppContainer(context: Context) {
    private val database: AppDatabase = AppDatabase.getDatabase(context)

    val taskInstanceDao: TaskInstanceDao = database.taskInstanceDao()
    val dailyTaskTemplateDao: DailyTaskTemplateDao = database.dailyTaskTemplateDao()
    val rewardDao: RewardDao = database.rewardDao()
    val redemptionDao: RedemptionDao = database.redemptionDao()
    val tagDao: TagDao = database.tagDao()

    val walletDataStore: WalletDataStore = WalletDataStore(context)
    val appSettingsDataStore: AppSettingsDataStore = AppSettingsDataStore(context)
}
