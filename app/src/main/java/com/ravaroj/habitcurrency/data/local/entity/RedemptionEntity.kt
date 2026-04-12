package com.ravaroj.habitcurrency.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "redemptions")
data class RedemptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rewardId: Long,
    val rewardTitleSnapshot: String,
    val cost: Int,
    val date: String, // yyyy-MM-dd
    val isDaily: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
