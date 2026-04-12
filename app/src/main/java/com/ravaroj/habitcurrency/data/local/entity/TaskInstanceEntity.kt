package com.ravaroj.habitcurrency.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_instances",
    indices = [
        Index(value = ["date", "displayOrder", "createdAt"])
    ]
)
data class TaskInstanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val rewardValue: Int,
    val date: String, // yyyy-MM-dd
    val isCompleted: Boolean = false,
    val isCarriedForward: Boolean = false,
    val isFromDailyTemplate: Boolean = false,
    val templateId: Long? = null,
    val claimCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val displayOrder: Int = 0
)
