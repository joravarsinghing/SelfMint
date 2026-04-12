package com.ravaroj.habitcurrency.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ravaroj.habitcurrency.HabitCurrencyApplication
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskInstanceEntity
import com.ravaroj.habitcurrency.data.repository.TaskRepository
import com.ravaroj.habitcurrency.ui.tasks.TaskFilterStatus
import com.ravaroj.habitcurrency.ui.tasks.TaskFilterType
import com.ravaroj.habitcurrency.ui.tasks.TaskSortOption
import com.ravaroj.habitcurrency.ui.tasks.TaskType
import com.ravaroj.habitcurrency.ui.tasks.TasksUiState
import com.ravaroj.habitcurrency.ui.tasks.TasksViewModel
import com.ravaroj.habitcurrency.ui.tasks.TasksViewModelFactory
import com.ravaroj.habitcurrency.ui.theme.InactiveNavGrey
import com.ravaroj.habitcurrency.ui.theme.TasksBlue

@Composable
fun TasksScreen() {
    val context = LocalContext.current.applicationContext
    val app = context as HabitCurrencyApplication
    val appContainer = app.appContainer

    val viewModel: TasksViewModel = viewModel(
        factory = remember(appContainer) {
            TasksViewModelFactory(
                TaskRepository(
                    taskInstanceDao = appContainer.taskInstanceDao,
                    dailyTaskTemplateDao = appContainer.dailyTaskTemplateDao,
                    tagDao = appContainer.tagDao
                ),
                appContainer.walletDataStore
            )
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCompletedToday by rememberSaveable { mutableStateOf(false) }
    var isReorderMode by rememberSaveable { mutableStateOf(false) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }

    val isManualSort = uiState.filterState.sort == TaskSortOption.MANUAL_ORDER
    val activeTasks = remember(uiState.visibleTasks) { uiState.visibleTasks.filter { !it.isCompleted } }
    val completedTasks = remember(uiState.visibleTasks) { uiState.visibleTasks.filter { it.isCompleted } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // A) HEADER ROW (FIXED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Wallet: $${uiState.walletBalance}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TasksBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // B) BUTTON ROW (Reorder + Filter)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Reorder / Done Button (1/3 width)
                        OutlinedButton(
                            onClick = { if (isManualSort) isReorderMode = !isReorderMode },
                            modifier = Modifier
                                .weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(if (isReorderMode) "Done" else "Reorder")
                        }

                        // Filter Button (2/3 width)
                        OutlinedButton(
                            onClick = { showFilterDialog = true },
                            modifier = Modifier
                                .weight(2f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(if (uiState.filterState.isDefault) "Filter" else "Filter (On)")
                        }
                    }
                }

                // D) ACTIVE TASKS SECTION
                item {
                    Text(
                        text = "Active Tasks",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (activeTasks.isEmpty()) {
                    item {
                        Text(
                            text = "No active tasks.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(activeTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            tags = uiState.taskTags[task.id].orEmpty(),
                            isReorderMode = isReorderMode,
                            onMoveUp = { viewModel.moveTaskUp(task) },
                            onMoveDown = { viewModel.moveTaskDown(task) },
                            onToggle = { viewModel.toggleTaskCompletion(task) },
                            onEdit = { viewModel.editTask(task) },
                            onDelete = { viewModel.deleteTask(task) },
                            onRepeat = { viewModel.repeatDailyTask(task) }
                        )
                    }
                }

                // E) COMPLETED TODAY (COLLAPSIBLE)
                item {
                    OutlinedButton(
                        onClick = { showCompletedToday = !showCompletedToday },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(if (showCompletedToday) "Hide Completed Today" else "Show Completed Today (${completedTasks.size})")
                    }
                }

                if (showCompletedToday) {
                    if (completedTasks.isEmpty()) {
                        item {
                            Text(
                                text = "No tasks completed today.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(completedTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                tags = uiState.taskTags[task.id].orEmpty(),
                                isReorderMode = isReorderMode,
                                onMoveUp = { viewModel.moveTaskUp(task) },
                                onMoveDown = { viewModel.moveTaskDown(task) },
                                onToggle = { viewModel.toggleTaskCompletion(task) },
                                onEdit = { viewModel.editTask(task) },
                                onDelete = { viewModel.deleteTask(task) },
                                onRepeat = { viewModel.repeatDailyTask(task) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.toggleAddSection() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = TasksBlue,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    if (uiState.isAddExpanded) {
        AddTaskDialog(
            uiState = uiState,
            onTitleChange = viewModel::onTitleChanged,
            onRewardChange = viewModel::onRewardChanged,
            onTypeSelect = viewModel::onTaskTypeSelected,
            onTagSelectionToggle = viewModel::toggleTagSelection,
            onShowTagManager = viewModel::showTagManagerDialog,
            onSave = viewModel::saveTask,
            onDismiss = { viewModel.toggleAddSection() }
        )
    }

    if (uiState.showTagManagerDialog) {
        TagManagerDialog(
            uiState = uiState,
            onNameChange = viewModel::onTagNameChanged,
            onColorSelect = viewModel::onTagColorSelected,
            onSave = viewModel::addTag,
            onDismiss = viewModel::hideTagManagerDialog
        )
    }

    if (showFilterDialog) {
        TaskFilterDialog(
            uiState = uiState,
            onTagToggle = viewModel::toggleFilterTag,
            onTypeSelect = viewModel::onFilterTypeSelected,
            onStatusSelect = { status ->
                viewModel.onFilterStatusSelected(status)
                showCompletedToday = status != TaskFilterStatus.ACTIVE_ONLY
            },
            onSortSelect = {
                viewModel.onSortSelected(it)
                if (it != TaskSortOption.MANUAL_ORDER) isReorderMode = false
            },
            onReset = {
                viewModel.resetFilters()
                isReorderMode = false
                showCompletedToday = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
private fun TaskFilterDialog(
    uiState: TasksUiState,
    onTagToggle: (Long) -> Unit,
    onTypeSelect: (TaskFilterType) -> Unit,
    onStatusSelect: (TaskFilterStatus) -> Unit,
    onSortSelect: (TaskSortOption) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1F1F23),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Tasks", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
                item {
                    Text("Tags", style = MaterialTheme.typography.titleMedium, color = TasksBlue)
                }
                if (uiState.tags.isEmpty()) {
                    item {
                        Text("No tags yet.", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                } else {
                    items(uiState.tags, key = { "filter_tag_${it.id}" }) { tag ->
                        FilterCheckRow(
                            label = tag.name,
                            checked = tag.id in uiState.filterState.selectedTagIds,
                            accent = TasksBlue,
                            onClick = { onTagToggle(tag.id) }
                        )
                    }
                }
                item {
                    Text("Type", style = MaterialTheme.typography.titleMedium, color = TasksBlue)
                }
                items(TaskFilterType.entries, key = { "task_type_${it.name}" }) { type ->
                    FilterCheckRow(
                        label = type.label,
                        checked = uiState.filterState.type == type,
                        accent = TasksBlue,
                        onClick = { onTypeSelect(type) }
                    )
                }
                item {
                    Text("Status", style = MaterialTheme.typography.titleMedium, color = TasksBlue)
                }
                items(TaskFilterStatus.entries, key = { "task_status_${it.name}" }) { status ->
                    FilterCheckRow(
                        label = status.label,
                        checked = uiState.filterState.status == status,
                        accent = TasksBlue,
                        onClick = { onStatusSelect(status) }
                    )
                }
                item {
                    Text("Sort", style = MaterialTheme.typography.titleMedium, color = TasksBlue)
                }
                items(TaskSortOption.entries, key = { "task_sort_${it.name}" }) { sort ->
                    FilterCheckRow(
                        label = sort.label,
                        checked = uiState.filterState.sort == sort,
                        accent = TasksBlue,
                        onClick = { onSortSelect(sort) }
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReset,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, TasksBlue),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TasksBlue, contentColor = Color.Black)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCheckRow(
    label: String,
    checked: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(checkedColor = accent, uncheckedColor = Color.White)
        )
        Text(label, color = Color.White, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AddTaskDialog(
    uiState: TasksUiState,
    onTitleChange: (String) -> Unit,
    onRewardChange: (String) -> Unit,
    onTypeSelect: (TaskType) -> Unit,
    onTagSelectionToggle: (Long) -> Unit,
    onShowTagManager: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var isDialogExpanded by rememberSaveable { mutableStateOf(false) }
    var showAdaptableRewardsInfo by rememberSaveable { mutableStateOf(false) }
    var tagsExpanded by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with X
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Task",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Normal Mode Fields
                OutlinedTextField(
                    value = uiState.titleInput,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task title") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TasksBlue,
                        focusedLabelColor = TasksBlue,
                        cursorColor = TasksBlue
                    )
                )

                OutlinedTextField(
                    value = uiState.rewardInput,
                    onValueChange = onRewardChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reward value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TasksBlue,
                        focusedLabelColor = TasksBlue,
                        cursorColor = TasksBlue
                    )
                )

                // Expand/Collapse
                TextButton(
                    onClick = { isDialogExpanded = !isDialogExpanded },
                    modifier = Modifier.align(Alignment.Start),
                    colors = ButtonDefaults.textButtonColors(contentColor = TasksBlue)
                ) {
                    Text(if (isDialogExpanded) "Collapse" else "Expand options")
                }

                if (isDialogExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // One-Time / Daily selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onTypeSelect(TaskType.ONE_TIME) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (uiState.selectedTaskType == TaskType.ONE_TIME) TasksBlue.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(if (uiState.selectedTaskType == TaskType.ONE_TIME) "✓ One-Time" else "One-Time")
                            }

                            OutlinedButton(
                                onClick = { onTypeSelect(TaskType.DAILY) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (uiState.selectedTaskType == TaskType.DAILY) TasksBlue.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(if (uiState.selectedTaskType == TaskType.DAILY) "✓ Daily" else "Daily")
                            }
                        }

                        Text("Tags", style = MaterialTheme.typography.bodyLarge)
                        if (uiState.selectedTagIds.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                uiState.tags
                                    .filter { it.id in uiState.selectedTagIds }
                                    .take(3)
                                    .forEach { tag ->
                                        TagChip(tag = tag)
                                    }
                                if (uiState.selectedTagIds.size > 3) {
                                    Text("+${uiState.selectedTagIds.size - 3}")
                                }
                            }
                        }
                        Column {
                            OutlinedButton(
                                onClick = { tagsExpanded = !tagsExpanded },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text("Select Tags")
                            }
                            if (tagsExpanded) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 260.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 3.dp
                                ) {
                                    LazyColumn {
                                        item {
                                            DropdownMenuItem(
                                                text = { Text("+ Add Tag") },
                                                onClick = {
                                                    tagsExpanded = false
                                                    onShowTagManager()
                                                }
                                            )
                                        }
                                        items(uiState.tags, key = { it.id }) { tag ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Checkbox(
                                                            checked = tag.id in uiState.selectedTagIds,
                                                            onCheckedChange = null,
                                                            colors = CheckboxDefaults.colors(checkedColor = TasksBlue)
                                                        )
                                                        TagChip(tag = tag)
                                                    }
                                                },
                                                onClick = { onTagSelectionToggle(tag.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (uiState.tags.size >= 10) {
                            Text(
                                text = "Max 10 tags",
                                style = MaterialTheme.typography.bodySmall,
                                color = InactiveNavGrey
                            )
                        }

                        // Adaptable rewards switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Adaptable rewards", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Future logic",
                                    tint = TasksBlue,
                                    modifier = Modifier
                                        .height(18.dp)
                                        .clickable { showAdaptableRewardsInfo = true }
                                )
                            }
                            Switch(
                                checked = true,
                                onCheckedChange = { /* Future logic */ },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = TasksBlue
                                )
                            )
                        }
                    }
                }

                // Save button
                Button(
                    onClick = onSave,
                    modifier = Modifier.align(Alignment.End),
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TasksBlue,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save Task")
                }
            }
        }
    }

    if (showAdaptableRewardsInfo) {
        AlertDialog(
            onDismissRequest = { showAdaptableRewardsInfo = false },
            title = { Text("Adaptable rewards*") },
            text = {
                Text(
                    "Future feature: SelfMint will suggest reward values based on your task history, consistency, and missed days. It is currently only a placeholder setting."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showAdaptableRewardsInfo = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TasksBlue)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun TagManagerDialog(
    uiState: TasksUiState,
    onNameChange: (String) -> Unit,
    onColorSelect: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tag") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.tagNameInput,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tag name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TasksBlue,
                        focusedLabelColor = TasksBlue,
                        cursorColor = TasksBlue
                    )
                )
                Text("Color", style = MaterialTheme.typography.bodyMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TasksViewModel.DEFAULT_TAG_COLORS.chunked(5).forEach { colors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            colors.forEach { colorHex ->
                                val isSelected = uiState.selectedTagColor == colorHex
                                val color = colorFromHex(colorHex)
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(28.dp)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = Color.White,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .background(color, RoundedCornerShape(14.dp))
                                        .clickable { onColorSelect(colorHex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Text(
                                            text = "✓",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                uiState.tagLimitError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = TasksBlue
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                colors = ButtonDefaults.textButtonColors(contentColor = TasksBlue)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TagChip(tag: TagEntity) {
    Row(
        modifier = Modifier
            .background(colorFromHex(tag.colorHex).copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .background(colorFromHex(tag.colorHex), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = tag.name, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TaskCard(
    task: TaskInstanceEntity,
    tags: List<TagEntity> = emptyList(),
    isReorderMode: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRepeat: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isFromDailyTemplate)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isReorderMode && !task.isCompleted) {
                    Column(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.height(24.dp)) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up")
                        }
                        IconButton(onClick = onMoveDown, modifier = Modifier.height(24.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down")
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val typeText = if (task.isFromDailyTemplate) " • Daily" else ""
                    val countText = if (task.isFromDailyTemplate && task.claimCount > 1) " • x${task.claimCount}" else ""
                    Text(
                        text = "Reward: $${task.rewardValue}$typeText$countText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (task.isCarriedForward) {
                        Text(
                            text = "Carry Forward",
                            style = MaterialTheme.typography.bodySmall,
                            color = InactiveNavGrey,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isReorderMode) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggle() },
                            colors = CheckboxDefaults.colors(checkedColor = TasksBlue)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            if (task.isFromDailyTemplate && task.isCompleted) {
                                DropdownMenuItem(
                                    text = { Text("Repeat") },
                                    onClick = {
                                        menuExpanded = false
                                        onRepeat()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(2.dp)
                ) {
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(colorFromHex(tag.colorHex))
                        )
                    }
                }
            }
        }
    }
}

private fun colorFromHex(colorHex: String): Color {
    return Color(android.graphics.Color.parseColor(colorHex))
}
