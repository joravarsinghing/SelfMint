package com.ravaroj.habitcurrency.ui.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravaroj.habitcurrency.data.local.WalletDataStore
import com.ravaroj.habitcurrency.data.local.entity.RedemptionEntity
import com.ravaroj.habitcurrency.data.local.entity.RewardEntity
import com.ravaroj.habitcurrency.data.repository.RewardRepository
import com.ravaroj.habitcurrency.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RewardsViewModel(
    private val rewardRepository: RewardRepository,
    private val walletDataStore: WalletDataStore
) : ViewModel() {

    private val today = DateUtils.todayString()

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    init {
        observeWallet()
        observeRewards()
        observeTodayRedemptions()
        observeTags()
        observeRewardTagLinks()
    }

    private fun observeWallet() {
        viewModelScope.launch {
            walletDataStore.balanceFlow.collect { balance ->
                _uiState.update { it.copy(walletBalance = balance) }
            }
        }
    }

    private fun observeRewards() {
        viewModelScope.launch {
            rewardRepository.getActiveRewards().collect { rewards ->
                _uiState.update { it.copy(rewards = rewards).withVisibleRewards() }
            }
        }
    }

    private fun observeTodayRedemptions() {
        viewModelScope.launch {
            rewardRepository.getTodayRedemptions(today).collect { redemptions ->
                _uiState.update { it.copy(recentRedemptions = redemptions).withVisibleRewards() }
            }
        }
    }

    private fun observeTags() {
        viewModelScope.launch {
            rewardRepository.observeTags().collect { tags ->
                _uiState.update { it.copy(tags = tags).withRewardTags().withVisibleRewards() }
            }
        }
    }

    private fun observeRewardTagLinks() {
        viewModelScope.launch {
            rewardRepository.observeRewardTagLinks().collect { links ->
                _uiState.update { it.copy(rewardTagLinks = links).withRewardTags().withVisibleRewards() }
            }
        }
    }

    private fun RewardsUiState.withRewardTags(): RewardsUiState {
        val tagsById = tags.associateBy { it.id }
        val rewardTags = rewardTagLinks
            .groupBy { it.rewardId }
            .mapValues { (_, links) -> links.mapNotNull { tagsById[it.tagId] } }
        return copy(rewardTags = rewardTags)
    }

    private fun RewardsUiState.withVisibleRewards(): RewardsUiState {
        val filteredRewards = rewards
            .filter { reward ->
                filterState.selectedTagIds.isEmpty() ||
                    rewardTags[reward.id].orEmpty().any { it.id in filterState.selectedTagIds }
            }
            .filter { reward ->
                when (filterState.type) {
                    RewardFilterType.ALL -> true
                    RewardFilterType.ONE_TIME -> !reward.isPermanent
                    RewardFilterType.DAILY -> reward.isPermanent
                }
            }
            .let { rewards ->
                when (filterState.sort) {
                    RewardSortOption.MANUAL_ORDER -> rewards
                    RewardSortOption.ALPHABETICAL -> rewards.sortedBy { it.title.lowercase() }
                    RewardSortOption.COST_HIGH_LOW -> rewards.sortedByDescending { it.cost }
                    RewardSortOption.COST_LOW_HIGH -> rewards.sortedBy { it.cost }
                    RewardSortOption.DAILY_FIRST -> rewards.sortedWith(
                        compareByDescending<RewardEntity> { it.isPermanent }
                            .thenBy { it.displayOrder }
                            .thenByDescending { it.createdAt }
                    )
                    RewardSortOption.ONE_TIME_FIRST -> rewards.sortedWith(
                        compareBy<RewardEntity> { it.isPermanent }
                            .thenBy { it.displayOrder }
                            .thenByDescending { it.createdAt }
                    )
                }
            }

        val filteredRedemptions = recentRedemptions
            .filter { filterState.selectedTagIds.isEmpty() }
            .filter { redemption ->
                when (filterState.type) {
                    RewardFilterType.ALL -> true
                    RewardFilterType.ONE_TIME -> !redemption.isDaily
                    RewardFilterType.DAILY -> redemption.isDaily
                }
            }
            .let { redemptions ->
                when (filterState.sort) {
                    RewardSortOption.MANUAL_ORDER -> redemptions
                    RewardSortOption.ALPHABETICAL -> redemptions.sortedBy { it.rewardTitleSnapshot.lowercase() }
                    RewardSortOption.COST_HIGH_LOW -> redemptions.sortedByDescending { it.cost }
                    RewardSortOption.COST_LOW_HIGH -> redemptions.sortedBy { it.cost }
                    RewardSortOption.DAILY_FIRST -> redemptions.sortedByDescending { it.isDaily }
                    RewardSortOption.ONE_TIME_FIRST -> redemptions.sortedBy { it.isDaily }
                }
            }

        return when (filterState.status) {
            RewardFilterStatus.ACTIVE_REWARDS -> copy(
                visibleRewards = filteredRewards,
                visibleRedemptions = emptyList()
            )
            RewardFilterStatus.REDEEMED_TODAY -> copy(
                visibleRewards = emptyList(),
                visibleRedemptions = filteredRedemptions
            )
            RewardFilterStatus.ALL -> copy(
                visibleRewards = filteredRewards,
                visibleRedemptions = filteredRedemptions
            )
        }
    }

    fun toggleAddSection() {
        _uiState.update { it.copy(isAddExpanded = !it.isAddExpanded) }
    }

    fun onRewardTitleChanged(value: String) {
        _uiState.update { it.copy(rewardTitleInput = value) }
    }

    fun onRewardCostChanged(value: String) {
        _uiState.update { it.copy(rewardCostInput = value) }
    }

    fun onRewardTypeSelected(type: RewardType) {
        _uiState.update { it.copy(selectedRewardType = type) }
    }

    fun toggleFilterTag(tagId: Long) {
        _uiState.update { current ->
            val selectedTagIds = if (tagId in current.filterState.selectedTagIds) {
                current.filterState.selectedTagIds - tagId
            } else {
                current.filterState.selectedTagIds + tagId
            }
            current.copy(filterState = current.filterState.copy(selectedTagIds = selectedTagIds))
                .withVisibleRewards()
        }
    }

    fun onFilterTypeSelected(type: RewardFilterType) {
        _uiState.update { it.copy(filterState = it.filterState.copy(type = type)).withVisibleRewards() }
    }

    fun onFilterStatusSelected(status: RewardFilterStatus) {
        _uiState.update { it.copy(filterState = it.filterState.copy(status = status)).withVisibleRewards() }
    }

    fun onSortSelected(sort: RewardSortOption) {
        _uiState.update { it.copy(filterState = it.filterState.copy(sort = sort)).withVisibleRewards() }
    }

    fun resetFilters() {
        _uiState.update { it.copy(filterState = RewardFilterState()).withVisibleRewards() }
    }

    fun saveReward() {
        val state = _uiState.value
        val title = state.rewardTitleInput.trim().replaceFirstChar { it.uppercase() }
        if (title.isBlank()) return

        val cost = state.rewardCostInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val isPermanent = state.selectedRewardType == RewardType.DAILY

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            rewardRepository.addReward(
                title = title,
                cost = cost,
                isPermanent = isPermanent,
                tagIds = state.selectedTagIds.toList()
            )

            _uiState.update {
                it.copy(
                    rewardTitleInput = "",
                    rewardCostInput = "",
                    selectedRewardType = RewardType.ONE_TIME,
                    selectedTagIds = emptySet(),
                    isSaving = false
                )
            }
        }
    }

    fun prefillRewardForEdit(reward: RewardEntity) {
        _uiState.update {
            it.copy(
                rewardTitleInput = reward.title,
                rewardCostInput = reward.cost.toString(),
                selectedRewardType = if (reward.isPermanent) RewardType.DAILY else RewardType.ONE_TIME,
                selectedTagIds = it.rewardTagLinks
                    .filter { link -> link.rewardId == reward.id }
                    .map { link -> link.tagId }
                    .toSet(),
                isAddExpanded = true
            )
        }
    }

    fun editReward(reward: RewardEntity) {
        val title = reward.title
        val cost = reward.cost
        val type = if (reward.isPermanent) RewardType.DAILY else RewardType.ONE_TIME

        _uiState.update {
            it.copy(
                rewardTitleInput = title,
                rewardCostInput = cost.toString(),
                selectedRewardType = type,
                selectedTagIds = it.rewardTagLinks
                    .filter { link -> link.rewardId == reward.id }
                    .map { link -> link.tagId }
                    .toSet(),
                isAddExpanded = true
            )
        }

        viewModelScope.launch {
            rewardRepository.deleteReward(reward)
        }
    }

    fun toggleTagSelection(tagId: Long) {
        _uiState.update { current ->
            val selectedTagIds = if (tagId in current.selectedTagIds) {
                current.selectedTagIds - tagId
            } else {
                current.selectedTagIds + tagId
            }
            current.copy(selectedTagIds = selectedTagIds)
        }
    }

    fun clearSelectedTagsAfterSave() {
        _uiState.update { it.copy(selectedTagIds = emptySet()) }
    }

    fun addTag(name: String, colorHex: String) {
        viewModelScope.launch {
            rewardRepository.addTag(name, colorHex)
        }
    }

    fun updateTag(tag: com.ravaroj.habitcurrency.data.local.entity.TagEntity) {
        viewModelScope.launch {
            rewardRepository.updateTag(tag)
        }
    }

    fun deleteTag(tag: com.ravaroj.habitcurrency.data.local.entity.TagEntity) {
        viewModelScope.launch {
            rewardRepository.deleteTag(tag)
        }
    }

    fun deleteReward(reward: RewardEntity) {
        viewModelScope.launch {
            rewardRepository.deleteReward(reward)
        }
    }

    fun redeemReward(reward: RewardEntity) {
        val currentBalance = _uiState.value.walletBalance
        if (currentBalance < reward.cost) return

        viewModelScope.launch {
            walletDataStore.subtract(reward.cost)
            rewardRepository.redeemReward(reward, today)

            if (!reward.isPermanent) {
                rewardRepository.deleteReward(reward)
            }
        }
    }

    fun editRedemption(redemption: RedemptionEntity) {
        _uiState.update {
            it.copy(
                rewardTitleInput = redemption.rewardTitleSnapshot,
                rewardCostInput = redemption.cost.toString(),
                selectedRewardType = if (redemption.isDaily) RewardType.DAILY else RewardType.ONE_TIME,
                isAddExpanded = true
            )
        }

        viewModelScope.launch {
            walletDataStore.add(redemption.cost)
            val restoredId = rewardRepository.prepareRedemptionForEdit(redemption)
            if (restoredId != null) {
                rewardRepository.deleteRewardById(restoredId)
            }
        }
    }

    fun deleteRedemption(redemption: RedemptionEntity) {
        viewModelScope.launch {
            walletDataStore.add(redemption.cost)
            rewardRepository.undoRedemption(redemption)
        }
    }

    fun undoRedemption(redemption: RedemptionEntity) {
        viewModelScope.launch {
            walletDataStore.add(redemption.cost)
            rewardRepository.undoRedemption(redemption)
        }
    }

    fun moveRewardUp(reward: RewardEntity) {
        if (_uiState.value.filterState.sort != RewardSortOption.MANUAL_ORDER) return
        val rewards = _uiState.value.visibleRewards
        val index = rewards.indexOf(reward)
        if (index > 0) {
            val prevReward = rewards[index - 1]
            swapRewards(reward, prevReward)
        }
    }

    fun moveRewardDown(reward: RewardEntity) {
        if (_uiState.value.filterState.sort != RewardSortOption.MANUAL_ORDER) return
        val rewards = _uiState.value.visibleRewards
        val index = rewards.indexOf(reward)
        if (index < rewards.size - 1) {
            val nextReward = rewards[index + 1]
            swapRewards(reward, nextReward)
        }
    }

    private fun swapRewards(reward1: RewardEntity, reward2: RewardEntity) {
        viewModelScope.launch {
            rewardRepository.swapRewardOrder(reward1, reward2)
        }
    }
}
