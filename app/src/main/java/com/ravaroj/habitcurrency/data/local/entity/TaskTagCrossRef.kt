package com.ravaroj.habitcurrency.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["tagId"])
    ]
)
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long
)
