package com.ravaroj.habitcurrency.data.repository

import com.ravaroj.habitcurrency.data.local.dao.RedemptionDao
import com.ravaroj.habitcurrency.data.local.dao.RewardDao
import com.ravaroj.habitcurrency.data.local.dao.TagDao
import com.ravaroj.habitcurrency.data.local.entity.RedemptionEntity
import com.ravaroj.habitcurrency.data.local.entity.RewardEntity
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

class RewardRepository(
    private val rewardDao: RewardDao,
    private val redemptionDao: RedemptionDao,
    private val tagDao: TagDao
) {

    fun getActiveRewards(): Flow<List<RewardEntity>> = rewardDao.getActiveRewards()

    fun getRecentRedemptions(limit: Int = 20): Flow<List<RedemptionEntity>> =
        redemptionDao.getRecentRedemptions(limit)

    fun getTodayRedemptions(date: String): Flow<List<RedemptionEntity>> =
        redemptionDao.getRedemptionsForDate(date)

    fun observeTags(): Flow<List<TagEntity>> = tagDao.observeTags()

    suspend fun addReward(
        title: String,
        cost: Int,
        isPermanent: Boolean
    ) {
        val cleanTitle = title.trim().replaceFirstChar { it.uppercase() }
        if (cleanTitle.isBlank()) return

        val maxOrder = rewardDao.getMaxDisplayOrder() ?: 0

        rewardDao.insert(
            RewardEntity(
                title = cleanTitle,
                cost = cost.coerceAtLeast(0),
                isPermanent = isPermanent,
                isActive = true,
                displayOrder = maxOrder + 1
            )
        )
    }

    suspend fun updateReward(
        reward: RewardEntity,
        newTitle: String,
        newCost: Int,
        newIsPermanent: Boolean
    ) {
        val cleanTitle = newTitle.trim().replaceFirstChar { it.uppercase() }
        if (cleanTitle.isBlank()) return

        rewardDao.update(
            reward.copy(
                title = cleanTitle,
                cost = newCost.coerceAtLeast(0),
                isPermanent = newIsPermanent
            )
        )
    }

    suspend fun deleteReward(reward: RewardEntity) {
        rewardDao.delete(reward)
    }

    suspend fun redeemReward(
        reward: RewardEntity,
        date: String
    ) {
        redemptionDao.insert(
            RedemptionEntity(
                rewardId = reward.id,
                rewardTitleSnapshot = reward.title,
                cost = reward.cost,
                date = date,
                isDaily = reward.isPermanent
            )
        )
    }

    suspend fun undoRedemption(redemption: RedemptionEntity) {
        if (!redemption.isDaily) {
            val maxOrder = rewardDao.getMaxDisplayOrder() ?: 0
            rewardDao.insert(
                RewardEntity(
                    title = redemption.rewardTitleSnapshot,
                    cost = redemption.cost,
                    isPermanent = false,
                    isActive = true,
                    displayOrder = maxOrder + 1
                )
            )
        }
        redemptionDao.delete(redemption)
    }

    suspend fun prepareRedemptionForEdit(redemption: RedemptionEntity): Long? {
        val restoredId = if (!redemption.isDaily) {
            val maxOrder = rewardDao.getMaxDisplayOrder() ?: 0
            rewardDao.insert(
                RewardEntity(
                    title = redemption.rewardTitleSnapshot,
                    cost = redemption.cost,
                    isPermanent = false,
                    isActive = true,
                    displayOrder = maxOrder + 1
                )
            )
        } else {
            null
        }
        redemptionDao.delete(redemption)
        return restoredId
    }

    suspend fun deleteRewardById(id: Long) {
        rewardDao.deleteById(id)
    }

    suspend fun updateReward(reward: RewardEntity) {
        rewardDao.update(reward)
    }

    suspend fun swapRewardOrder(reward1: RewardEntity, reward2: RewardEntity) {
        rewardDao.swapDisplayOrders(reward1, reward2)
    }
}
