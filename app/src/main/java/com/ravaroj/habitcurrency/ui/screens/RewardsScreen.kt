package com.ravaroj.habitcurrency.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.ravaroj.habitcurrency.data.local.entity.RedemptionEntity
import com.ravaroj.habitcurrency.data.local.entity.RewardEntity
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.data.repository.RewardRepository
import com.ravaroj.habitcurrency.ui.tasks.TasksViewModel
import com.ravaroj.habitcurrency.ui.rewards.RewardFilterStatus
import com.ravaroj.habitcurrency.ui.rewards.RewardFilterType
import com.ravaroj.habitcurrency.ui.rewards.RewardSortOption
import com.ravaroj.habitcurrency.ui.rewards.RewardType
import com.ravaroj.habitcurrency.ui.rewards.RewardsUiState
import com.ravaroj.habitcurrency.ui.rewards.RewardsViewModel
import com.ravaroj.habitcurrency.ui.rewards.RewardsViewModelFactory
import com.ravaroj.habitcurrency.ui.theme.InactiveNavGrey
import com.ravaroj.habitcurrency.ui.theme.RewardsOrange

@Composable
fun RewardsScreen() {
    val context = LocalContext.current.applicationContext
    val app = context as HabitCurrencyApplication
    val appContainer = app.appContainer

    val viewModel: RewardsViewModel = viewModel(
        factory = remember(appContainer) {
            RewardsViewModelFactory(
                rewardRepository = RewardRepository(
                    rewardDao = appContainer.rewardDao,
                    redemptionDao = appContainer.redemptionDao,
                    tagDao = appContainer.tagDao
                ),
                walletDataStore = appContainer.walletDataStore
            )
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRecentRedemptions by rememberSaveable { mutableStateOf(false) }
    var isReorderMode by rememberSaveable { mutableStateOf(false) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    val isManualSort = uiState.filterState.sort == RewardSortOption.MANUAL_ORDER
    var showTagManager by rememberSaveable { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<TagEntity?>(null) }
    var tagNameInput by remember { mutableStateOf("") }
    var selectedTagColor by remember { mutableStateOf(TasksViewModel.DEFAULT_TAG_COLORS.first()) }
    var tagError by remember { mutableStateOf<String?>(null) }

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
                    text = "Rewards",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Wallet: $${uiState.walletBalance}",
                    style = MaterialTheme.typography.titleMedium,
                    color = RewardsOrange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // B) BUTTON ROW (Reorder + Filter)
                item(key = "button_row", contentType = "button") {
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

                // D) REWARDS LIST (MERGED)
                item(key = "rewards_header", contentType = "header") {
                    Text(
                        text = "Rewards",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (uiState.visibleRewards.isEmpty()) {
                    item(key = "empty_rewards", contentType = "info") {
                        Text(
                            text = "No rewards available.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(
                        items = uiState.visibleRewards,
                        key = { "reward_${it.id}" },
                        contentType = { "reward_card" }
                    ) { reward ->
                        RewardCard(
                            reward = reward,
                            tags = uiState.rewardTags[reward.id].orEmpty(),
                            isReorderMode = isReorderMode,
                            onMoveUp = { viewModel.moveRewardUp(reward) },
                            onMoveDown = { viewModel.moveRewardDown(reward) },
                            onRedeem = { viewModel.redeemReward(reward) },
                            onEdit = { viewModel.editReward(reward) },
                            onDelete = { viewModel.deleteReward(reward) }
                        )
                    }
                }

                // E) RECENT REDEMPTIONS (COLLAPSIBLE)
                item(key = "redemptions_toggle", contentType = "button") {
                    OutlinedButton(
                        onClick = { showRecentRedemptions = !showRecentRedemptions },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(if (showRecentRedemptions) "Hide Today's Redemptions" else "Show Today's Redemptions (${uiState.visibleRedemptions.size})")
                    }
                }

                if (showRecentRedemptions) {
                    if (uiState.visibleRedemptions.isEmpty()) {
                        item(key = "empty_redemptions", contentType = "info") {
                            Text(
                                text = "No redemptions today.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.visibleRedemptions,
                            key = { "redemption_${it.id}" },
                            contentType = { "redemption_card" }
                        ) { redemption ->
                            RedemptionCard(
                                redemption = redemption,
                                isReorderMode = isReorderMode,
                                onEdit = { viewModel.editRedemption(redemption) },
                                onDelete = { viewModel.deleteRedemption(redemption) },
                                onUndo = { viewModel.undoRedemption(redemption) }
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
            containerColor = RewardsOrange,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Reward")
        }
    }

    if (uiState.isAddExpanded) {
        AddRewardDialog(
            uiState = uiState,
            onTitleChange = viewModel::onRewardTitleChanged,
            onCostChange = viewModel::onRewardCostChanged,
            onTypeSelect = viewModel::onRewardTypeSelected,
            onTagSelectionToggle = viewModel::toggleTagSelection,
            onShowTagManager = { showTagManager = true },
            onSave = viewModel::saveReward,
            onDismiss = { viewModel.toggleAddSection() }
        )
    }

    if (showFilterDialog) {
        RewardFilterDialog(
            uiState = uiState,
            onTagToggle = viewModel::toggleFilterTag,
            onTypeSelect = viewModel::onFilterTypeSelected,
            onStatusSelect = { status ->
                viewModel.onFilterStatusSelected(status)
                showRecentRedemptions = status != RewardFilterStatus.ACTIVE_REWARDS
            },
            onSortSelect = { sort ->
                viewModel.onSortSelected(sort)
                if (sort != RewardSortOption.MANUAL_ORDER) isReorderMode = false
            },
            onReset = {
                viewModel.resetFilters()
                isReorderMode = false
                showRecentRedemptions = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

}

@Composable
fun AddRewardDialog(
    uiState: RewardsUiState,
    onTitleChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onTypeSelect: (RewardType) -> Unit,
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
                        text = "Add Reward",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Normal Mode Fields
                OutlinedTextField(
                    value = uiState.rewardTitleInput,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reward title") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RewardsOrange,
                        focusedLabelColor = RewardsOrange,
                        cursorColor = RewardsOrange
                    )
                )

                OutlinedTextField(
                    value = uiState.rewardCostInput,
                    onValueChange = onCostChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reward cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RewardsOrange,
                        focusedLabelColor = RewardsOrange,
                        cursorColor = RewardsOrange
                    )
                )

                // Expand/Collapse
                TextButton(
                    onClick = { isDialogExpanded = !isDialogExpanded },
                    modifier = Modifier.align(Alignment.Start),
                    colors = ButtonDefaults.textButtonColors(contentColor = RewardsOrange)
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
                                onClick = { onTypeSelect(RewardType.ONE_TIME) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (uiState.selectedRewardType == RewardType.ONE_TIME) RewardsOrange.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(if (uiState.selectedRewardType == RewardType.ONE_TIME) "✓ One-Time" else "One-Time")
                            }

                            OutlinedButton(
                                onClick = { onTypeSelect(RewardType.DAILY) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (uiState.selectedRewardType == RewardType.DAILY) RewardsOrange.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(if (uiState.selectedRewardType == RewardType.DAILY) "✓ Daily" else "Daily")
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
                                        RewardTagChip(tag = tag)
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
                                border = BorderStroke(1.dp, RewardsOrange),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = RewardsOrange
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Select Tags")
                                    Icon(
                                        imageVector = if (tagsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                }
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
                                                            colors = CheckboxDefaults.colors(checkedColor = RewardsOrange)
                                                        )
                                                        RewardTagChip(tag = tag)
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
                                    tint = RewardsOrange,
                                    modifier = Modifier
                                        .height(18.dp)
                                        .clickable { showAdaptableRewardsInfo = true }
                                )
                            }
                            Switch(
                                checked = false,
                                onCheckedChange = { /* Future logic */ },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = RewardsOrange
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
                        containerColor = RewardsOrange,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save Reward")
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
                    colors = ButtonDefaults.textButtonColors(contentColor = RewardsOrange)
                ) {
                    Text("OK")
                }
            }
        )
    }

}

@Composable
private fun RewardFilterDialog(
    uiState: RewardsUiState,
    onTagToggle: (Long) -> Unit,
    onTypeSelect: (RewardFilterType) -> Unit,
    onStatusSelect: (RewardFilterStatus) -> Unit,
    onSortSelect: (RewardSortOption) -> Unit,
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
                        Text("Filter Rewards", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
                item {
                    Text("Tags", style = MaterialTheme.typography.titleMedium, color = RewardsOrange)
                }
                if (uiState.tags.isEmpty()) {
                    item {
                        Text("No tags yet.", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                } else {
                    items(uiState.tags, key = { "reward_filter_tag_${it.id}" }) { tag ->
                        RewardFilterCheckRow(
                            label = tag.name,
                            checked = tag.id in uiState.filterState.selectedTagIds,
                            accent = RewardsOrange,
                            onClick = { onTagToggle(tag.id) }
                        )
                    }
                }
                item {
                    Text("Type", style = MaterialTheme.typography.titleMedium, color = RewardsOrange)
                }
                items(RewardFilterType.entries, key = { "reward_type_${it.name}" }) { type ->
                    RewardFilterCheckRow(
                        label = type.label,
                        checked = uiState.filterState.type == type,
                        accent = RewardsOrange,
                        onClick = { onTypeSelect(type) }
                    )
                }
                item {
                    Text("Status", style = MaterialTheme.typography.titleMedium, color = RewardsOrange)
                }
                items(RewardFilterStatus.entries, key = { "reward_status_${it.name}" }) { status ->
                    RewardFilterCheckRow(
                        label = status.label,
                        checked = uiState.filterState.status == status,
                        accent = RewardsOrange,
                        onClick = { onStatusSelect(status) }
                    )
                }
                item {
                    Text("Sort", style = MaterialTheme.typography.titleMedium, color = RewardsOrange)
                }
                items(RewardSortOption.entries, key = { "reward_sort_${it.name}" }) { sort ->
                    RewardFilterCheckRow(
                        label = sort.label,
                        checked = uiState.filterState.sort == sort,
                        accent = RewardsOrange,
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
                            border = BorderStroke(1.dp, RewardsOrange),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = RewardsOrange, contentColor = Color.Black)
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
private fun RewardTagChip(tag: TagEntity) {
    Row(
        modifier = Modifier
            .background(rewardColorFromHex(tag.colorHex).copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .background(rewardColorFromHex(tag.colorHex), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = tag.name, style = MaterialTheme.typography.bodySmall)
    }
}

private fun rewardColorFromHex(colorHex: String): Color {
    return Color(android.graphics.Color.parseColor(colorHex))
}

@Composable
private fun RewardTagManagerDialog(
    uiState: RewardsUiState,
    name: String,
    selectedColor: String,
    error: String?,
    onNameChange: (String) -> Unit,
    onColorSelect: (String) -> Unit,
    onAdd: () -> Unit,
    onEdit: (TagEntity) -> Unit,
    onDelete: (TagEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Tags") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
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
                                        .background(rewardColorFromHex(colorHex), RoundedCornerShape(14.dp))
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
                error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = RewardsOrange) }
                OutlinedButton(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.tags.size < RewardRepository.MAX_TAGS,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RewardsOrange),
                    border = BorderStroke(1.dp, RewardsOrange)
                ) {
                    Text("+ Add Tag")
                }

                Spacer(modifier = Modifier.height(8.dp))
                uiState.tags.forEach { tag ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RewardTagChip(tag = tag)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { onEdit(tag) },
                                colors = ButtonDefaults.textButtonColors(contentColor = RewardsOrange)
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
                if (uiState.tags.size >= RewardRepository.MAX_TAGS) {
                    Text("Max 10 tags", style = MaterialTheme.typography.bodySmall, color = RewardsOrange)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun RewardTagEditDialog(
    tag: TagEntity,
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
        title = { Text("Edit Tag") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
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
                                        .background(rewardColorFromHex(colorHex), RoundedCornerShape(14.dp))
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
                error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = RewardsOrange) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                colors = ButtonDefaults.textButtonColors(contentColor = RewardsOrange)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RewardFilterCheckRow(
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
private fun RewardCard(
    reward: RewardEntity,
    tags: List<TagEntity> = emptyList(),
    isReorderMode: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onRedeem: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val containerColor = if (reward.isPermanent)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    else
        MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isReorderMode) {
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
                        text = reward.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val typeText = if (reward.isPermanent) " • Daily" else ""
                    Text(
                        text = "Reward cost: $${reward.cost}$typeText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isReorderMode) {
                        Button(
                            onClick = onRedeem,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RewardsOrange,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Redeem")
                        }
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
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
                                .background(rewardColorFromHex(tag.colorHex))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RedemptionCard(
    redemption: RedemptionEntity,
    isReorderMode: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUndo: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val containerColor = if (redemption.isDaily)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    else
        MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = redemption.rewardTitleSnapshot,
                    style = MaterialTheme.typography.titleMedium
                )
                val typeText = if (redemption.isDaily) " • Daily" else ""
                Text(
                    text = "Reward cost: $${redemption.cost}$typeText",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isReorderMode) {
                    Checkbox(
                        checked = true,
                        onCheckedChange = { onUndo() },
                        colors = CheckboxDefaults.colors(checkedColor = RewardsOrange)
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
    }
}
