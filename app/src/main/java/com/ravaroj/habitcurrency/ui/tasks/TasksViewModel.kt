package com.ravaroj.habitcurrency.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravaroj.habitcurrency.data.local.WalletDataStore
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskInstanceEntity
import com.ravaroj.habitcurrency.data.repository.TaskRepository
import com.ravaroj.habitcurrency.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val taskRepository: TaskRepository,
    private val walletDataStore: WalletDataStore
) : ViewModel() {

    private val today = DateUtils.todayString()

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        observeTodayTasks()
        observeWallet()
        observeTags()
        observeTaskTagLinks()
    }

    private fun observeTodayTasks() {
        viewModelScope.launch {
            taskRepository.getTasksForDate(today).collect { tasks ->
                _uiState.update { current ->
                    current.copy(tasks = tasks).withVisibleTasks()
                }
            }
        }
    }

    private fun observeWallet() {
        viewModelScope.launch {
            walletDataStore.balanceFlow.collect { balance ->
                _uiState.update { it.copy(walletBalance = balance) }
            }
        }
    }

    private fun observeTags() {
        viewModelScope.launch {
            taskRepository.observeTags().collect { tags ->
                _uiState.update { it.copy(tags = tags).withTaskTags().withVisibleTasks() }
            }
        }
    }

    private fun observeTaskTagLinks() {
        viewModelScope.launch {
            taskRepository.observeTaskTagLinks().collect { links ->
                _uiState.update { it.copy(taskTagLinks = links).withTaskTags().withVisibleTasks() }
            }
        }
    }

    private fun TasksUiState.withTaskTags(): TasksUiState {
        val tagsById = tags.associateBy { it.id }
        val taskTags = taskTagLinks
            .groupBy { it.taskId }
            .mapValues { (_, links) -> links.mapNotNull { tagsById[it.tagId] } }
        return copy(taskTags = taskTags)
    }

    private fun TasksUiState.withVisibleTasks(): TasksUiState {
        val visible = tasks
            .filter { task ->
                filterState.selectedTagIds.isEmpty() ||
                    taskTags[task.id].orEmpty().any { it.id in filterState.selectedTagIds }
            }
            .filter { task ->
                when (filterState.type) {
                    TaskFilterType.ALL -> true
                    TaskFilterType.ONE_TIME -> !task.isFromDailyTemplate
                    TaskFilterType.DAILY -> task.isFromDailyTemplate
                }
            }
            .filter { task ->
                when (filterState.status) {
                    TaskFilterStatus.ACTIVE_ONLY -> !task.isCompleted
                    TaskFilterStatus.COMPLETED_TODAY -> task.isCompleted
                    TaskFilterStatus.LATE_CARRY_FORWARD -> task.isCarriedForward
                    TaskFilterStatus.ALL -> true
                }
            }
            .let { tasks ->
                when (filterState.sort) {
                    TaskSortOption.MANUAL_ORDER -> tasks
                    TaskSortOption.ALPHABETICAL -> tasks.sortedBy { it.title.lowercase() }
                    TaskSortOption.REWARD_HIGH_LOW -> tasks.sortedByDescending { it.rewardValue }
                    TaskSortOption.REWARD_LOW_HIGH -> tasks.sortedBy { it.rewardValue }
                    TaskSortOption.DAILY_FIRST -> tasks.sortedWith(
                        compareByDescending<TaskInstanceEntity> { it.isFromDailyTemplate }
                            .thenBy { it.displayOrder }
                            .thenByDescending { it.createdAt }
                    )
                    TaskSortOption.ONE_TIME_FIRST -> tasks.sortedWith(
                        compareBy<TaskInstanceEntity> { it.isFromDailyTemplate }
                            .thenBy { it.displayOrder }
                            .thenByDescending { it.createdAt }
                    )
                }
            }
        return copy(visibleTasks = visible)
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(titleInput = value) }
    }

    fun onRewardChanged(value: String) {
        _uiState.update { it.copy(rewardInput = value) }
    }

    fun onTaskTypeSelected(taskType: TaskType) {
        _uiState.update { it.copy(selectedTaskType = taskType) }
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

    fun showTagManagerDialog() {
        _uiState.update {
            it.copy(
                showTagManagerDialog = true,
                tagNameInput = "",
                selectedTagColor = DEFAULT_TAG_COLORS.first(),
                tagLimitError = if (it.tags.size >= TaskRepository.MAX_TAGS) "Max 10 tags" else null
            )
        }
    }

    fun hideTagManagerDialog() {
        _uiState.update {
            it.copy(
                showTagManagerDialog = false,
                tagNameInput = "",
                selectedTagColor = DEFAULT_TAG_COLORS.first(),
                tagLimitError = null
            )
        }
    }

    fun onTagNameChanged(value: String) {
        _uiState.update { it.copy(tagNameInput = value, tagLimitError = null) }
    }

    fun onTagColorSelected(colorHex: String) {
        _uiState.update { it.copy(selectedTagColor = colorHex) }
    }

    fun addTag() {
        val state = _uiState.value
        if (state.tags.size >= TaskRepository.MAX_TAGS) {
            _uiState.update { it.copy(tagLimitError = "Max 10 tags") }
            return
        }

        viewModelScope.launch {
            val added = taskRepository.addTag(
                name = state.tagNameInput,
                colorHex = state.selectedTagColor
            )
            _uiState.update {
                if (added) {
                    it.copy(
                        showTagManagerDialog = false,
                        tagNameInput = "",
                        selectedTagColor = DEFAULT_TAG_COLORS.first(),
                        tagLimitError = null
                    )
                } else {
                    it.copy(tagLimitError = "Could not add tag")
                }
            }
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch {
            taskRepository.deleteTag(tag)
            _uiState.update {
                it.copy(
                    selectedTagIds = it.selectedTagIds - tag.id,
                    filterState = it.filterState.copy(selectedTagIds = it.filterState.selectedTagIds - tag.id)
                ).withVisibleTasks()
            }
        }
    }

    fun toggleFilterTag(tagId: Long) {
        _uiState.update { current ->
            val selectedTagIds = if (tagId in current.filterState.selectedTagIds) {
                current.filterState.selectedTagIds - tagId
            } else {
                current.filterState.selectedTagIds + tagId
            }
            current.copy(filterState = current.filterState.copy(selectedTagIds = selectedTagIds))
                .withVisibleTasks()
        }
    }

    fun onFilterTypeSelected(type: TaskFilterType) {
        _uiState.update { it.copy(filterState = it.filterState.copy(type = type)).withVisibleTasks() }
    }

    fun onFilterStatusSelected(status: TaskFilterStatus) {
        _uiState.update { it.copy(filterState = it.filterState.copy(status = status)).withVisibleTasks() }
    }

    fun onSortSelected(sort: TaskSortOption) {
        _uiState.update { it.copy(filterState = it.filterState.copy(sort = sort)).withVisibleTasks() }
    }

    fun resetFilters() {
        _uiState.update { it.copy(filterState = TaskFilterState()).withVisibleTasks() }
    }

    fun clearSelectedTagsAfterSave() {
        _uiState.update { it.copy(selectedTagIds = emptySet()) }
    }

    fun toggleAddSection() {
        _uiState.update { it.copy(isAddExpanded = !it.isAddExpanded) }
    }

    fun editTask(task: TaskInstanceEntity) {
        val title = task.title
        val reward = task.rewardValue
        val taskType = if (task.isFromDailyTemplate) TaskType.DAILY else TaskType.ONE_TIME

        _uiState.update {
            it.copy(
                titleInput = title,
                rewardInput = reward.toString(),
                selectedTaskType = taskType,
                selectedTagIds = it.taskTagLinks
                    .filter { link -> link.taskId == task.id }
                    .map { link -> link.tagId }
                    .toSet(),
                isAddExpanded = true
            )
        }

        viewModelScope.launch {
            if (task.isCompleted) {
                val totalRefund = task.rewardValue * task.claimCount.coerceAtLeast(1)
                walletDataStore.subtract(totalRefund)
            }
            taskRepository.prepareTaskForEdit(task)
        }
    }

    fun saveTask() {
        val currentState = _uiState.value
        val cleanTitle = currentState.titleInput.trim().replaceFirstChar { it.uppercase() }
        if (cleanTitle.isBlank()) return

        val rewardValue = currentState.rewardInput.toIntOrNull()?.coerceAtLeast(0) ?: 0

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            when (currentState.selectedTaskType) {
                TaskType.ONE_TIME -> {
                    taskRepository.addOneTimeTask(
                        title = cleanTitle,
                        rewardValue = rewardValue,
                        date = today,
                        tagIds = currentState.selectedTagIds.toList()
                    )
                }
                TaskType.DAILY -> {
                    taskRepository.addDailyTask(
                        title = cleanTitle,
                        rewardValue = rewardValue,
                        date = today,
                        tagIds = currentState.selectedTagIds.toList()
                    )
                }
            }

            _uiState.update {
                it.copy(
                    titleInput = "",
                    rewardInput = "",
                    selectedTaskType = TaskType.ONE_TIME,
                    selectedTagIds = emptySet(),
                    isSaving = false
                )
            }
        }
    }

    fun toggleTaskCompletion(task: TaskInstanceEntity) {
        viewModelScope.launch {
            if (task.isCompleted) {
                if (task.isFromDailyTemplate && task.claimCount > 1) {
                    taskRepository.reduceDailyClaim(task)
                } else {
                    taskRepository.markTaskUncompleted(task)
                }
                walletDataStore.subtract(task.rewardValue)
            } else {
                taskRepository.markTaskCompleted(task)
                walletDataStore.add(task.rewardValue)
            }
        }
    }

    fun repeatDailyTask(task: TaskInstanceEntity) {
        if (!task.isFromDailyTemplate || !task.isCompleted) return

        viewModelScope.launch {
            taskRepository.repeatDailyTask(task)
            walletDataStore.add(task.rewardValue)
        }
    }

    fun deleteTask(task: TaskInstanceEntity) {
        viewModelScope.launch {
            if (task.isCompleted) {
                val totalDeduct = task.rewardValue * task.claimCount.coerceAtLeast(1)
                walletDataStore.subtract(totalDeduct)
            }
            taskRepository.deleteTask(task)
        }
    }

    fun moveTaskUp(task: TaskInstanceEntity) {
        if (_uiState.value.filterState.sort != TaskSortOption.MANUAL_ORDER) return
        val tasks = _uiState.value.visibleTasks.filter { !it.isCompleted }
        val index = tasks.indexOf(task)
        if (index > 0) {
            val prevTask = tasks[index - 1]
            swapTasks(task, prevTask)
        }
    }

    fun moveTaskDown(task: TaskInstanceEntity) {
        if (_uiState.value.filterState.sort != TaskSortOption.MANUAL_ORDER) return
        val tasks = _uiState.value.visibleTasks.filter { !it.isCompleted }
        val index = tasks.indexOf(task)
        if (index < tasks.size - 1) {
            val nextTask = tasks[index + 1]
            swapTasks(task, nextTask)
        }
    }

    private fun swapTasks(task1: TaskInstanceEntity, task2: TaskInstanceEntity) {
        viewModelScope.launch {
            taskRepository.swapTaskOrder(task1, task2)
        }
    }

    companion object {
        val DEFAULT_TAG_COLORS = listOf(
            "#2196F3",
            "#FF9800",
            "#4CAF50",
            "#F44336",
            "#9C27B0",
            "#FFEB3B",
            "#00BCD4",
            "#E91E63",
            "#9E9E9E",
            "#FFFFFF"
        )
    }
}
