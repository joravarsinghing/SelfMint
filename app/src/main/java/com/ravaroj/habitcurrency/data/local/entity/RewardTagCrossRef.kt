package com.ravaroj.habitcurrency.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "reward_tag_cross_ref",
    primaryKeys = ["rewardId", "tagId"],
    indices = [
        Index(value = ["rewardId"]),
        Index(value = ["tagId"])
    ]
)
data class RewardTagCrossRef(
    val rewardId: Long,
    val tagId: Long
)
