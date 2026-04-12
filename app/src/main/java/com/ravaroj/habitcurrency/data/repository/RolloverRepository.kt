package com.ravaroj.habitcurrency.data.repository

import com.ravaroj.habitcurrency.data.local.AppSettingsDataStore
import com.ravaroj.habitcurrency.data.local.dao.DailyTaskTemplateDao
import com.ravaroj.habitcurrency.data.local.dao.RedemptionDao
import com.ravaroj.habitcurrency.data.local.dao.TaskInstanceDao
import com.ravaroj.habitcurrency.data.local.entity.TaskInstanceEntity
import com.ravaroj.habitcurrency.util.DateUtils
import kotlinx.coroutines.flow.first

class RolloverRepository(
    private val taskInstanceDao: TaskInstanceDao,
    private val dailyTaskTemplateDao: DailyTaskTemplateDao,
    private val redemptionDao: RedemptionDao,
    private val appSettingsDataStore: AppSettingsDataStore
) {

    suspend fun processNewDayIfNeeded(today: String = DateUtils.todayString()) {
        val lastProcessedDate = appSettingsDataStore.lastProcessedDateFlow.first()

        if (lastProcessedDate == null) {
            generateDailyTasksForDate(today)
            cleanupOldData(today)
            appSettingsDataStore.setLastProcessedDate(today)
            return
        }

        if (lastProcessedDate == today) return

        val datesToProcess = DateUtils.datesBetweenExclusive(lastProcessedDate, today)

        if (datesToProcess.isEmpty()) {
            appSettingsDataStore.setLastProcessedDate(today)
            return
        }

        for (date in datesToProcess) {
            val previousDate = DateUtils.previousDate(date)
            carryForwardIncompleteOneTimeTasks(previousDate, date)
            generateDailyTasksForDate(date)
        }

        cleanupOldData(today)
        appSettingsDataStore.setLastProcessedDate(today)
    }

    private suspend fun carryForwardIncompleteOneTimeTasks(fromDate: String, toDate: String) {
        val incompleteTasks = taskInstanceDao.getIncompleteOneTimeTasksForDate(fromDate)
        val maxOrder = taskInstanceDao.getMaxDisplayOrder(toDate) ?: 0

        val carriedTasks = incompleteTasks.mapIndexed { index, task ->
            TaskInstanceEntity(
                title = task.title,
                rewardValue = task.rewardValue,
                date = toDate,
                isCompleted = false,
                isCarriedForward = true,
                isFromDailyTemplate = false,
                templateId = null,
                displayOrder = maxOrder + index + 1
            )
        }

        if (carriedTasks.isNotEmpty()) {
            taskInstanceDao.insertAll(carriedTasks)
        }
    }

    private suspend fun generateDailyTasksForDate(date: String) {
        val templates = dailyTaskTemplateDao.getActiveTemplatesOnce()
        var nextOrder = (taskInstanceDao.getMaxDisplayOrder(date) ?: 0) + 1

        val tasksToInsert = mutableListOf<TaskInstanceEntity>()

        for (template in templates) {
            val existingCount = taskInstanceDao.countInstancesForTemplateOnDate(template.id, date)
            if (existingCount == 0) {
                tasksToInsert.add(
                    TaskInstanceEntity(
                        title = template.title,
                        rewardValue = template.rewardValue,
                        date = date,
                        isCompleted = false,
                        isCarriedForward = false,
                        isFromDailyTemplate = true,
                        templateId = template.id,
                        displayOrder = nextOrder++
                    )
                )
            }
        }

        if (tasksToInsert.isNotEmpty()) {
            taskInstanceDao.insertAll(tasksToInsert)
        }
    }

    private suspend fun cleanupOldData(today: String) {
        val cutoffDate = DateUtils.minusDays(today, 7)
        taskInstanceDao.deleteOlderThan(cutoffDate)
        redemptionDao.deleteOlderThan(cutoffDate)
    }
}
