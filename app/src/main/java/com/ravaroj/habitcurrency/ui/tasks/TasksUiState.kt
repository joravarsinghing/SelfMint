package com.ravaroj.habitcurrency.ui.tasks

import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskInstanceEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskTagCrossRef

data class TasksUiState(
    val titleInput: String = "",
    val rewardInput: String = "",
    val selectedTaskType: TaskType = TaskType.ONE_TIME,
    val tasks: List<TaskInstanceEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val taskTagLinks: List<TaskTagCrossRef> = emptyList(),
    val taskTags: Map<Long, List<TagEntity>> = emptyMap(),
    val selectedTagIds: Set<Long> = emptySet(),
    val showTagManagerDialog: Boolean = false,
    val tagNameInput: String = "",
    val selectedTagColor: String = "#2196F3",
    val tagLimitError: String? = null,
    val isSaving: Boolean = false,
    val isAddExpanded: Boolean = false,
    val walletBalance: Int = 0,
    val filterState: TaskFilterState = TaskFilterState(),
    val visibleTasks: List<TaskInstanceEntity> = emptyList()
)

data class TaskFilterState(
    val selectedTagIds: Set<Long> = emptySet(),
    val type: TaskFilterType = TaskFilterType.ALL,
    val status: TaskFilterStatus = TaskFilterStatus.ALL,
    val sort: TaskSortOption = TaskSortOption.MANUAL_ORDER
) {
    val isDefault: Boolean
        get() = selectedTagIds.isEmpty() &&
            type == TaskFilterType.ALL &&
            status == TaskFilterStatus.ALL &&
            sort == TaskSortOption.MANUAL_ORDER
}

enum class TaskFilterType(val label: String) {
    ALL("All"),
    ONE_TIME("One-Time"),
    DAILY("Daily")
}

enum class TaskFilterStatus(val label: String) {
    ACTIVE_ONLY("Active only"),
    COMPLETED_TODAY("Completed today"),
    LATE_CARRY_FORWARD("Late / Carry-forward"),
    ALL("All")
}

enum class TaskSortOption(val label: String) {
    MANUAL_ORDER("Manual order"),
    ALPHABETICAL("Alphabetical A-Z"),
    REWARD_HIGH_LOW("Reward high-low"),
    REWARD_LOW_HIGH("Reward low-high"),
    DAILY_FIRST("Daily first"),
    ONE_TIME_FIRST("One-Time first")
}
