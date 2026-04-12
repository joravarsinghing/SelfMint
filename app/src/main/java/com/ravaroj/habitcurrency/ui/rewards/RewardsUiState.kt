package com.ravaroj.habitcurrency.ui.rewards

import com.ravaroj.habitcurrency.data.local.entity.RedemptionEntity
import com.ravaroj.habitcurrency.data.local.entity.RewardEntity
import com.ravaroj.habitcurrency.data.local.entity.TagEntity

data class RewardsUiState(
    val rewardTitleInput: String = "",
    val rewardCostInput: String = "",
    val selectedRewardType: RewardType = RewardType.ONE_TIME,
    val isAddExpanded: Boolean = false,
    val walletBalance: Int = 0,
    val rewards: List<RewardEntity> = emptyList(),
    val recentRedemptions: List<RedemptionEntity> = emptyList(),
    val isSaving: Boolean = false,
    val tags: List<TagEntity> = emptyList(),
    val filterState: RewardFilterState = RewardFilterState(),
    val visibleRewards: List<RewardEntity> = emptyList(),
    val visibleRedemptions: List<RedemptionEntity> = emptyList()
)

data class RewardFilterState(
    val selectedTagIds: Set<Long> = emptySet(),
    val type: RewardFilterType = RewardFilterType.ALL,
    val status: RewardFilterStatus = RewardFilterStatus.ACTIVE_REWARDS,
    val sort: RewardSortOption = RewardSortOption.MANUAL_ORDER
) {
    val isDefault: Boolean
        get() = selectedTagIds.isEmpty() &&
            type == RewardFilterType.ALL &&
            status == RewardFilterStatus.ACTIVE_REWARDS &&
            sort == RewardSortOption.MANUAL_ORDER
}

enum class RewardFilterType(val label: String) {
    ALL("All"),
    ONE_TIME("One-Time"),
    DAILY("Daily")
}

enum class RewardFilterStatus(val label: String) {
    ACTIVE_REWARDS("Active rewards"),
    REDEEMED_TODAY("Redeemed today"),
    ALL("All")
}

enum class RewardSortOption(val label: String) {
    MANUAL_ORDER("Manual order"),
    ALPHABETICAL("Alphabetical A-Z"),
    COST_HIGH_LOW("Cost high-low"),
    COST_LOW_HIGH("Cost low-high"),
    DAILY_FIRST("Daily first"),
    ONE_TIME_FIRST("One-Time first")
}
