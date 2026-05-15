package com.ravaroj.habitcurrency.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "daily_task_template_tag_cross_ref",
    primaryKeys = ["templateId", "tagId"],
    indices = [
        Index(value = ["templateId"]),
        Index(value = ["tagId"])
    ]
)
data class DailyTaskTemplateTagCrossRef(
    val templateId: Long,
    val tagId: Long
)
