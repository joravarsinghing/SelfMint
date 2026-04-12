package com.ravaroj.habitcurrency.data.repository

import com.ravaroj.habitcurrency.data.local.dao.DailyTaskTemplateDao
import com.ravaroj.habitcurrency.data.local.dao.TagDao
import com.ravaroj.habitcurrency.data.local.dao.TaskInstanceDao
import com.ravaroj.habitcurrency.data.local.entity.DailyTaskTemplateEntity
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskInstanceEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskTagCrossRef
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskInstanceDao: TaskInstanceDao,
    private val dailyTaskTemplateDao: DailyTaskTemplateDao,
    private val tagDao: TagDao
) {

    companion object {
        const val MAX_TAGS = 10
    }

    fun getTasksForDate(date: String): Flow<List<TaskInstanceEntity>> {
        return taskInstanceDao.getTasksForDate(date)
    }

    fun observeTags(): Flow<List<TagEntity>> = tagDao.observeTags()

    fun observeTaskTagLinks(): Flow<List<TaskTagCrossRef>> = tagDao.observeTaskTagLinks()

    suspend fun addOneTimeTask(
        title: String,
        rewardValue: Int,
        date: String,
        tagIds: List<Long> = emptyList()
    ) {
        val cleanTitle = title.trim().replaceFirstChar { it.uppercase() }
        if (cleanTitle.isBlank()) return

        val maxOrder = taskInstanceDao.getMaxDisplayOrder(date) ?: 0

        val taskId = taskInstanceDao.insert(
            TaskInstanceEntity(
                title = cleanTitle,
                rewardValue = rewardValue,
                date = date,
                isCompleted = false,
                isCarriedForward = false,
                isFromDailyTemplate = false,
                templateId = null,
                displayOrder = maxOrder + 1
            )
        )
        assignTagsToTask(taskId, tagIds)
    }

    suspend fun addDailyTask(
        title: String,
        rewardValue: Int,
        date: String,
        tagIds: List<Long> = emptyList()
    ) {
        val cleanTitle = title.trim().replaceFirstChar { it.uppercase() }
        if (cleanTitle.isBlank()) return

        val templateId = dailyTaskTemplateDao.insert(
            DailyTaskTemplateEntity(
                title = cleanTitle,
                rewardValue = rewardValue,
                isActive = true
            )
        )

        val maxOrder = taskInstanceDao.getMaxDisplayOrder(date) ?: 0

        val taskId = taskInstanceDao.insert(
            TaskInstanceEntity(
                title = cleanTitle,
                rewardValue = rewardValue,
                date = date,
                isCompleted = false,
                isCarriedForward = false,
                isFromDailyTemplate = true,
                templateId = templateId,
                displayOrder = maxOrder + 1
            )
        )
        assignTagsToTask(taskId, tagIds)
    }

    suspend fun addTag(name: String, colorHex: String): Boolean {
        val cleanName = name.trim().replaceFirstChar { it.uppercase() }
        if (cleanName.isBlank()) return false
        if (tagDao.getTagCount() >= MAX_TAGS) return false
        return tagDao.insertTag(
            TagEntity(
                name = cleanName,
                colorHex = colorHex,
            )
        ) != -1L
    }

    suspend fun updateTag(tag: TagEntity) {
        tagDao.updateTag(tag)
    }

    suspend fun deleteTag(tag: TagEntity) {
        tagDao.deleteTagAndLinks(tag)
    }

    suspend fun assignTagsToTask(taskId: Long, tagIds: List<Long>) {
        tagDao.replaceTaskTagLinks(taskId, tagIds.distinct())
    }

    suspend fun markTaskCompleted(task: TaskInstanceEntity) {
        taskInstanceDao.update(
            task.copy(
                isCompleted = true,
                claimCount = if (task.claimCount <= 0) 1 else task.claimCount
            )
        )
    }

    suspend fun reduceDailyClaim(task: TaskInstanceEntity) {
        val newCount = (task.claimCount - 1).coerceAtLeast(0)

        if (newCount == 0) {
            taskInstanceDao.update(
                task.copy(
                    isCompleted = false,
                    claimCount = 0
                )
            )
        } else {
            taskInstanceDao.update(
                task.copy(
                    claimCount = newCount,
                    isCompleted = true
                )
            )
        }
    }

    suspend fun repeatDailyTask(task: TaskInstanceEntity) {
        if (!task.isFromDailyTemplate) return
        if (!task.isCompleted) return

        taskInstanceDao.update(
            task.copy(
                claimCount = task.claimCount + 1
            )
        )
    }

    suspend fun markTaskUncompleted(task: TaskInstanceEntity) {
        taskInstanceDao.update(task.copy(isCompleted = false))
    }

    suspend fun deleteTask(task: TaskInstanceEntity) {
        tagDao.clearTaskTagLinks(task.id)
        if (task.isFromDailyTemplate && task.templateId != null) {
            dailyTaskTemplateDao.deleteById(task.templateId)
        }
        taskInstanceDao.delete(task)
    }

    suspend fun prepareTaskForEdit(task: TaskInstanceEntity) {
        tagDao.clearTaskTagLinks(task.id)
        if (task.isFromDailyTemplate && task.templateId != null) {
            dailyTaskTemplateDao.deleteById(task.templateId)
        }
        taskInstanceDao.delete(task)
    }

    suspend fun updateTask(task: TaskInstanceEntity) {
        taskInstanceDao.update(task)
    }

    suspend fun swapTaskOrder(task1: TaskInstanceEntity, task2: TaskInstanceEntity) {
        taskInstanceDao.swapDisplayOrders(task1, task2)
    }
}
