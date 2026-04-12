package com.ravaroj.habitcurrency.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import com.ravaroj.habitcurrency.HabitCurrencyApplication
import com.ravaroj.habitcurrency.data.repository.DashboardRepository
import com.ravaroj.habitcurrency.ui.dashboard.DashboardViewModel
import com.ravaroj.habitcurrency.ui.dashboard.DashboardViewModelFactory
import com.ravaroj.habitcurrency.ui.theme.DashboardLightGrey
import com.ravaroj.habitcurrency.ui.theme.DashboardWhite
import com.ravaroj.habitcurrency.ui.theme.RewardsOrange
import com.ravaroj.habitcurrency.ui.theme.TasksBlue
import com.ravaroj.habitcurrency.util.DateUtils
import androidx.compose.ui.Alignment
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ravaroj.habitcurrency.data.repository.DemoDataRepository
import com.ravaroj.habitcurrency.data.repository.TaskRepository
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.ui.tasks.TasksViewModel
import kotlinx.coroutines.launch

import com.ravaroj.habitcurrency.ui.dashboard.ActivityBarChart
import com.ravaroj.habitcurrency.ui.dashboard.WalletLineChart

@Composable
fun DashboardScreen() {
    val context = LocalContext.current.applicationContext
    val app = context as HabitCurrencyApplication
    val appContainer = app.appContainer

    val repository = DashboardRepository(
        taskInstanceDao = appContainer.taskInstanceDao,
        redemptionDao = appContainer.redemptionDao
    )

    val demoRepository = DemoDataRepository(
        taskInstanceDao = appContainer.taskInstanceDao,
        dailyTaskTemplateDao = appContainer.dailyTaskTemplateDao,
        rewardDao = appContainer.rewardDao,
        redemptionDao = appContainer.redemptionDao,
        tagDao = appContainer.tagDao,
        walletDataStore = appContainer.walletDataStore,
        appSettingsDataStore = appContainer.appSettingsDataStore
    )

    val taskRepository = remember(appContainer) {
        TaskRepository(
            taskInstanceDao = appContainer.taskInstanceDao,
            dailyTaskTemplateDao = appContainer.dailyTaskTemplateDao,
            tagDao = appContainer.tagDao
        )
    }

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            dashboardRepository = repository,
            demoDataRepository = demoRepository,
            walletDataStore = appContainer.walletDataStore
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val today = DateUtils.todayString()

    val lifecycleOwner = LocalLifecycleOwner.current

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showGenerateConfirm by remember { mutableStateOf(false) }
    var showEditTagsDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<TagEntity?>(null) }
    var tagNameInput by remember { mutableStateOf("") }
    var selectedTagColor by remember { mutableStateOf(TasksViewModel.DEFAULT_TAG_COLORS.first()) }
    var tagError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val tags by taskRepository.observeTags().collectAsState(initial = emptyList())

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Wallet: $${uiState.walletBalance}",
                style = MaterialTheme.typography.titleMedium,
                color = DashboardWhite
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Today Card + Settings
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = "Today - ${DateUtils.displayFull(today)}",
                        titleColor = DashboardWhite,
                        lines = listOf(
                            buildAnnotatedString {
                                append("Earned today: ")
                                withStyle(SpanStyle(color = TasksBlue)) {
                                    append("$${uiState.earnedToday}")
                                }
                            },
                            buildAnnotatedString {
                                append("Spent today: ")
                                withStyle(SpanStyle(color = RewardsOrange)) {
                                    append("$${uiState.spentToday}")
                                }
                            },
                            AnnotatedString("Active tasks: ${uiState.activeToday}"),
                            AnnotatedString("Completed tasks: ${uiState.completedToday}")
                        ),
                        modifier = Modifier.weight(3f)
                    )

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { showSettingsDialog = true },
                        colors = CardDefaults.cardColors()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚙", style = MaterialTheme.typography.headlineLarge)
                        }
                    }
                }
            }

            // 2. Activity Graph Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Activity (Last 7 Days)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DashboardWhite
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ActivityBarChart(
                            earned = uiState.last7DaysEarned.map { it.second }.reversed(),
                            spent = uiState.last7DaysSpent.map { it.second }.reversed(),
                            labels = uiState.labels
                        )
                    }
                }
            }

            // 3. Wallet Trend Graph Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Wallet Trend",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DashboardWhite
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        WalletLineChart(
                            values = uiState.walletHistory,
                            labels = uiState.labels
                        )
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Dashboard Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Demo Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedButton(
                        onClick = { showGenerateConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DashboardWhite),
                        border = BorderStroke(1.dp, DashboardWhite)
                    ) {
                        Text("Generate Demo Data")
                    }
                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DashboardWhite),
                        border = BorderStroke(1.dp, DashboardWhite)
                    ) {
                        Text("Clear Demo Data")
                    }
                    OutlinedButton(
                        onClick = {
                            showEditTagsDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DashboardWhite),
                        border = BorderStroke(1.dp, DashboardWhite)
                    ) {
                        Text("Edit Tags")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DashboardWhite)
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showEditTagsDialog) {
        EditTagsDialog(
            tags = tags,
            onEdit = { tag ->
                editingTag = tag
                tagNameInput = tag.name
                selectedTagColor = tag.colorHex
                tagError = null
                showTagDialog = true
            },
            onDelete = { tag ->
                scope.launch { taskRepository.deleteTag(tag) }
            },
            onAdd = {
                editingTag = null
                tagNameInput = ""
                selectedTagColor = TasksViewModel.DEFAULT_TAG_COLORS.first()
                tagError = if (tags.size >= TaskRepository.MAX_TAGS) "Max 10 tags" else null
                showTagDialog = true
            },
            onDismiss = { showEditTagsDialog = false }
        )
    }

    if (showTagDialog) {
        DashboardTagDialog(
            title = if (editingTag == null) "Add Tag" else "Edit Tag",
            name = tagNameInput,
            selectedColor = selectedTagColor,
            error = tagError,
            onNameChange = {
                tagNameInput = it
                tagError = null
            },
            onColorSelect = { selectedTagColor = it },
            onSave = {
                val cleanName = tagNameInput.trim().replaceFirstChar { it.uppercase() }
                if (cleanName.isBlank()) {
                    tagError = "Tag name required"
                    return@DashboardTagDialog
                }
                scope.launch {
                    val tag = editingTag
                    if (tag == null) {
                        val added = taskRepository.addTag(cleanName, selectedTagColor)
                        if (added) {
                            showTagDialog = false
                        } else {
                            tagError = "Could not add tag"
                        }
                    } else {
                        taskRepository.updateTag(tag.copy(name = cleanName, colorHex = selectedTagColor))
                        showTagDialog = false
                    }
                }
            },
            onDismiss = { showTagDialog = false }
        )
    }

    if (showGenerateConfirm) {
        AlertDialog(
            onDismissRequest = { showGenerateConfirm = false },
            title = { Text("Generate Demo Data") },
            text = { Text("This will replace current local data with demo data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.generateDemoData()
                        showGenerateConfirm = false
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DashboardWhite)
                ) {
                    Text("Generate")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGenerateConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DashboardWhite)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data") },
            text = { Text("This will clear all local data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearDemoData()
                        showClearConfirm = false
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DashboardWhite)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EditTagsDialog(
    tags: List<TagEntity>,
    onEdit: (TagEntity) -> Unit,
    onDelete: (TagEntity) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Tags")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tags, key = { it.id }) { tag ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height(10.dp)
                                        .background(dashboardColorFromHex(tag.colorHex), RoundedCornerShape(5.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tag.name)
                            }
                            Row {
                                TextButton(
                                    onClick = { onEdit(tag) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = TasksBlue)
                                ) {
                                    Text("Edit")
                                }
                                TextButton(
                                    onClick = { onDelete(tag) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
                if (tags.size >= TaskRepository.MAX_TAGS) {
                    Text("Max 10 tags", style = MaterialTheme.typography.bodySmall, color = DashboardWhite)
                }
                OutlinedButton(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tags.size < TaskRepository.MAX_TAGS,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TasksBlue),
                    border = BorderStroke(1.dp, TasksBlue)
                ) {
                    Text("+ Add Tag")
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun DashboardTagDialog(
    title: String,
    name: String,
    selectedColor: String,
    error: String?,
    onNameChange: (String) -> Unit,
    onColorSelect: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tag name") },
                    singleLine = true
                )
                Text("Color", style = MaterialTheme.typography.bodyMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TasksViewModel.DEFAULT_TAG_COLORS.chunked(5).forEach { colors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            colors.forEach { colorHex ->
                                val isSelected = selectedColor == colorHex
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(28.dp)
                                        .background(dashboardColorFromHex(colorHex), RoundedCornerShape(14.dp))
                                        .clickable { onColorSelect(colorHex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Text("✓", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
                error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = TasksBlue)
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
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = DashboardWhite)
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun dashboardColorFromHex(colorHex: String): Color {
    return Color(android.graphics.Color.parseColor(colorHex))
}

@Composable
private fun StatsCard(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    lines: List<AnnotatedString>
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            )
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
