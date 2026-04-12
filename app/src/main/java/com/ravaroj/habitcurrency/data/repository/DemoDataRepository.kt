package com.ravaroj.habitcurrency.data.repository

import com.ravaroj.habitcurrency.data.local.AppSettingsDataStore
import com.ravaroj.habitcurrency.data.local.WalletDataStore
import com.ravaroj.habitcurrency.data.local.dao.DailyTaskTemplateDao
import com.ravaroj.habitcurrency.data.local.dao.RedemptionDao
import com.ravaroj.habitcurrency.data.local.dao.RewardDao
import com.ravaroj.habitcurrency.data.local.dao.TagDao
import com.ravaroj.habitcurrency.data.local.dao.TaskInstanceDao
import com.ravaroj.habitcurrency.data.local.entity.DailyTaskTemplateEntity
import com.ravaroj.habitcurrency.data.local.entity.RedemptionEntity
import com.ravaroj.habitcurrency.data.local.entity.RewardEntity
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskInstanceEntity
import com.ravaroj.habitcurrency.util.DateUtils

class DemoDataRepository(
    private val taskInstanceDao: TaskInstanceDao,
    private val dailyTaskTemplateDao: DailyTaskTemplateDao,
    private val rewardDao: RewardDao,
    private val redemptionDao: RedemptionDao,
    private val tagDao: TagDao,
    private val walletDataStore: WalletDataStore,
    private val appSettingsDataStore: AppSettingsDataStore
) {
    suspend fun clearDemoData() {
        tagDao.clearAllTaskTagLinks()
        tagDao.clearAllRewardTagLinks()
        tagDao.clearAllTags()
        taskInstanceDao.clearAll()
        dailyTaskTemplateDao.clearAll()
        rewardDao.clearAll()
        redemptionDao.clearAll()
        walletDataStore.setBalance(0)
        appSettingsDataStore.clearLastProcessedDate()
    }

    suspend fun generateDemoData() {
        clearDemoData()

        val today = DateUtils.todayString()
        val tags = createDemoTags()
        val templates = createDailyTemplates()
        val rewards = createDemoRewards(tags)

        var totalEarned = 0
        var totalSpent = 0

        val dailySpending = listOf(15, 25, 0, 20, 35, 45, 25)

        for (i in 0..6) {
            val date = DateUtils.minusDays(today, (6 - i).toLong())
            totalSpent += dailySpending[i]
            totalEarned += generateCompletedTasksForDay(date, i, templates, tags)
            generateRedemptionsForDay(date, dailySpending[i], rewards)
        }

        generateActiveTasksForToday(today, templates, tags)

        walletDataStore.setBalance((totalEarned - totalSpent).coerceAtLeast(1))
        appSettingsDataStore.setLastProcessedDate(today)
    }

    private suspend fun createDemoTags(): DemoTags {
        val health = tagDao.insertTag(TagEntity(name = "Health", colorHex = "#4CAF50"))
        val learning = tagDao.insertTag(TagEntity(name = "Learning", colorHex = "#2196F3"))
        val work = tagDao.insertTag(TagEntity(name = "Work", colorHex = "#FFB300"))
        val rest = tagDao.insertTag(TagEntity(name = "Rest", colorHex = "#FF9800"))
        val personal = tagDao.insertTag(TagEntity(name = "Personal", colorHex = "#9C27B0"))
        return DemoTags(
            health = health,
            learning = learning,
            work = work,
            rest = rest,
            personal = personal
        )
    }

    private suspend fun createDailyTemplates(): DemoTemplates {
        val workout = dailyTaskTemplateDao.insert(
            DailyTaskTemplateEntity(title = "Workout", rewardValue = 30)
        )
        val read = dailyTaskTemplateDao.insert(
            DailyTaskTemplateEntity(title = "Read 5 pages", rewardValue = 10)
        )
        val plan = dailyTaskTemplateDao.insert(
            DailyTaskTemplateEntity(title = "Plan tomorrow", rewardValue = 10)
        )
        val walk = dailyTaskTemplateDao.insert(
            DailyTaskTemplateEntity(title = "Walk 20 minutes", rewardValue = 15)
        )
        return DemoTemplates(
            workout = workout,
            read = read,
            plan = plan,
            walk = walk
        )
    }

    private suspend fun createDemoRewards(tags: DemoTags): DemoRewards {
        val gaming = insertReward(
            RewardEntity(title = "30 min gaming", cost = 25, isPermanent = true, displayOrder = 1),
            tags.rest
        )
        val episode = insertReward(
            RewardEntity(title = "Watch 1 episode", cost = 20, isPermanent = true, displayOrder = 2),
            tags.rest
        )
        val coffee = insertReward(
            RewardEntity(title = "Coffee treat", cost = 15, isPermanent = false, displayOrder = 3),
            tags.personal
        )
        val outing = insertReward(
            RewardEntity(title = "Weekend outing", cost = 80, isPermanent = false, displayOrder = 4),
            tags.rest,
            tags.personal
        )
        val smallBuy = insertReward(
            RewardEntity(title = "Buy something small", cost = 120, isPermanent = false, displayOrder = 5),
            tags.personal
        )
        return DemoRewards(
            gaming = gaming,
            episode = episode,
            coffee = coffee,
            outing = outing,
            smallBuy = smallBuy
        )
    }

    private suspend fun insertReward(reward: RewardEntity, vararg tagIds: Long): Long {
        val rewardId = rewardDao.insert(reward)
        tagDao.replaceRewardTagLinks(rewardId, tagIds.toList())
        return rewardId
    }

    private suspend fun generateCompletedTasksForDay(
        date: String,
        dayIndex: Int,
        templates: DemoTemplates,
        tags: DemoTags
    ): Int {
        var order = 1
        var earned = 0
        insertTask(
            TaskInstanceEntity(
                templateId = templates.read,
                title = "Read 5 pages",
                rewardValue = 10,
                date = date,
                isCompleted = true,
                isFromDailyTemplate = true,
                claimCount = 1,
                displayOrder = order++
            ),
            tags.learning
        )
        earned += 10
        insertTask(
            TaskInstanceEntity(
                title = "Focus work block",
                rewardValue = 40,
                date = date,
                isCompleted = true,
                isFromDailyTemplate = false,
                claimCount = 1,
                displayOrder = order++
            ),
            tags.work
        )
        earned += 40
        if (dayIndex % 2 == 0) {
            insertTask(
                TaskInstanceEntity(
                    templateId = templates.workout,
                    title = "Workout",
                    rewardValue = 30,
                    date = date,
                    isCompleted = true,
                    isFromDailyTemplate = true,
                    claimCount = 1,
                    displayOrder = order++
                ),
                tags.health
            )
            earned += 30
        }
        if (dayIndex % 3 == 0) {
            insertTask(
                TaskInstanceEntity(
                    title = "Study AI tools",
                    rewardValue = 25,
                    date = date,
                    isCompleted = true,
                    isFromDailyTemplate = false,
                    claimCount = 1,
                    displayOrder = order
                ),
                tags.learning,
                tags.work
            )
            earned += 25
        }
        return earned
    }

    private suspend fun generateActiveTasksForToday(
        today: String,
        templates: DemoTemplates,
        tags: DemoTags
    ) {
        var nextOrder = (taskInstanceDao.getMaxDisplayOrder(today) ?: 0) + 1
        insertTask(
            TaskInstanceEntity(
                templateId = templates.workout,
                title = "Workout",
                rewardValue = 30,
                date = today,
                isCompleted = false,
                isFromDailyTemplate = true,
                displayOrder = nextOrder++
            ),
            tags.health
        )
        insertTask(
            TaskInstanceEntity(
                templateId = templates.read,
                title = "Read 5 pages",
                rewardValue = 10,
                date = today,
                isCompleted = false,
                isFromDailyTemplate = true,
                displayOrder = nextOrder++
            ),
            tags.learning
        )
        insertTask(
            TaskInstanceEntity(
                title = "Focus work block",
                rewardValue = 40,
                date = today,
                isCompleted = false,
                isFromDailyTemplate = false,
                displayOrder = nextOrder++
            ),
            tags.work
        )
        insertTask(
            TaskInstanceEntity(
                templateId = templates.plan,
                title = "Plan tomorrow",
                rewardValue = 10,
                date = today,
                isCompleted = false,
                isFromDailyTemplate = true,
                displayOrder = nextOrder++
            ),
            tags.personal,
            tags.work
        )
        insertTask(
            TaskInstanceEntity(
                title = "Review weekly goals",
                rewardValue = 20,
                date = today,
                isCompleted = false,
                isFromDailyTemplate = false,
                displayOrder = nextOrder++
            ),
            tags.personal
        )
        insertTask(
            TaskInstanceEntity(
                templateId = templates.walk,
                title = "Walk 20 minutes",
                rewardValue = 15,
                date = today,
                isCompleted = false,
                isFromDailyTemplate = true,
                displayOrder = nextOrder++
            ),
            tags.health
        )
        insertTask(
            TaskInstanceEntity(
                title = "Study AI tools",
                rewardValue = 25,
                date = today,
                isCompleted = false,
                isFromDailyTemplate = false,
                displayOrder = nextOrder++
            ),
            tags.learning,
            tags.work
        )
        insertTask(
            TaskInstanceEntity(
                title = "Carry forward invoice cleanup",
                rewardValue = 15,
                date = today,
                isCompleted = false,
                isCarriedForward = true,
                isFromDailyTemplate = false,
                displayOrder = nextOrder
            ),
            tags.work,
            tags.personal
        )
    }

    private suspend fun insertTask(task: TaskInstanceEntity, vararg tagIds: Long) {
        val taskId = taskInstanceDao.insert(task)
        tagDao.replaceTaskTagLinks(taskId, tagIds.toList())
    }

    private suspend fun generateRedemptionsForDay(
        date: String,
        amount: Int,
        rewards: DemoRewards
    ) {
        var remaining = amount
        while (remaining > 0) {
            val redemption = when {
                remaining >= 25 -> RedemptionEntity(
                    rewardId = rewards.gaming,
                    rewardTitleSnapshot = "30 min gaming",
                    cost = 25,
                    date = date,
                    isDaily = true
                )
                remaining >= 20 -> RedemptionEntity(
                    rewardId = rewards.episode,
                    rewardTitleSnapshot = "Watch 1 episode",
                    cost = 20,
                    date = date,
                    isDaily = true
                )
                remaining >= 15 -> RedemptionEntity(
                    rewardId = rewards.coffee,
                    rewardTitleSnapshot = "Coffee treat",
                    cost = 15,
                    date = date,
                    isDaily = false
                )
                else -> RedemptionEntity(
                    rewardId = rewards.smallBuy,
                    rewardTitleSnapshot = "Buy something small",
                    cost = remaining,
                    date = date,
                    isDaily = false
                )
            }
            redemptionDao.insert(redemption)
            remaining -= redemption.cost
        }
    }

    private data class DemoTags(
        val health: Long,
        val learning: Long,
        val work: Long,
        val rest: Long,
        val personal: Long
    )

    private data class DemoTemplates(
        val workout: Long,
        val read: Long,
        val plan: Long,
        val walk: Long
    )

    private data class DemoRewards(
        val gaming: Long,
        val episode: Long,
        val coffee: Long,
        val outing: Long,
        val smallBuy: Long
    )
}
