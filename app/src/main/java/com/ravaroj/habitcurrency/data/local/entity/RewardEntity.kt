package com.ravaroj.habitcurrency.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rewards",
    indices = [
        Index(value = ["isActive", "displayOrder", "createdAt"])
    ]
)
data class RewardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val cost: Int,
    val isPermanent: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val displayOrder: Int = 0
)
