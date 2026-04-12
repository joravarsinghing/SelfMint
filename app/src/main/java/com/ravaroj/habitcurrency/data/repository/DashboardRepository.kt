package com.ravaroj.habitcurrency.data.repository

import com.ravaroj.habitcurrency.data.local.dao.RedemptionDao
import com.ravaroj.habitcurrency.data.local.dao.TaskInstanceDao
import com.ravaroj.habitcurrency.util.DateUtils

data class DashboardStats(
    val earnedToday: Int = 0,
    val spentToday: Int = 0,
    val completedToday: Int = 0,
    val activeToday: Int = 0
)

class DashboardRepository(
    private val taskInstanceDao: TaskInstanceDao,
    private val redemptionDao: RedemptionDao
) {
    suspend fun getTodayStats(date: String = DateUtils.todayString()): DashboardStats {
        return DashboardStats(
            earnedToday = taskInstanceDao.getTotalEarnedForDate(date),
            spentToday = redemptionDao.getTotalSpentForDate(date),
            completedToday = taskInstanceDao.getCompletedCountForDate(date),
            activeToday = taskInstanceDao.getActiveCountForDate(date)
        )
    }

    suspend fun getLast7DaysEarned(date: String = DateUtils.todayString()): List<Pair<String, Int>> {
        val results = mutableListOf<Pair<String, Int>>()
        for (i in 0..6) {
            val day = DateUtils.minusDays(date, i.toLong())
            val earned = taskInstanceDao.getTotalEarnedForDate(day)
            results.add(day to earned)
        }
        return results
    }

    suspend fun getLast7DaysSpent(date: String = DateUtils.todayString()): List<Pair<String, Int>> {
        val results = mutableListOf<Pair<String, Int>>()
        for (i in 0..6) {
            val day = DateUtils.minusDays(date, i.toLong())
            val spent = redemptionDao.getTotalSpentForDate(day)
            results.add(day to spent)
        }
        return results
    }
}
