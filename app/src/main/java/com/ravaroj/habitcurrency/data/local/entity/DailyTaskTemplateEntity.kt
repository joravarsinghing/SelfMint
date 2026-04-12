package com.ravaroj.habitcurrency.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_task_templates")
data class DailyTaskTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val rewardValue: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
