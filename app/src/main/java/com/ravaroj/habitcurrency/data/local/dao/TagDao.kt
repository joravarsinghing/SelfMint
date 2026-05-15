package com.ravaroj.habitcurrency.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ravaroj.habitcurrency.data.local.entity.DailyTaskTemplateTagCrossRef
import com.ravaroj.habitcurrency.data.local.entity.RewardTagCrossRef
import com.ravaroj.habitcurrency.data.local.entity.TagEntity
import com.ravaroj.habitcurrency.data.local.entity.TaskTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY createdAt ASC, name ASC")
    fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM task_tag_cross_ref")
    fun observeTaskTagLinks(): Flow<List<TaskTagCrossRef>>

    @Query("SELECT * FROM reward_tag_cross_ref")
    fun observeRewardTagLinks(): Flow<List<RewardTagCrossRef>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int

    @Query("DELETE FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun clearTaskTagLinks(taskId: Long)

    @Query("DELETE FROM task_tag_cross_ref WHERE tagId = :tagId")
    suspend fun clearLinksForTag(tagId: Long)

    @Query("DELETE FROM task_tag_cross_ref")
    suspend fun clearAllTaskTagLinks()

    @Query("DELETE FROM reward_tag_cross_ref WHERE rewardId = :rewardId")
    suspend fun clearRewardTagLinks(rewardId: Long)

    @Query("DELETE FROM reward_tag_cross_ref WHERE tagId = :tagId")
    suspend fun clearRewardLinksForTag(tagId: Long)

    @Query("DELETE FROM reward_tag_cross_ref")
    suspend fun clearAllRewardTagLinks()

    @Query("DELETE FROM tags")
    suspend fun clearAllTags()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskTagLinks(links: List<TaskTagCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardTagLinks(links: List<RewardTagCrossRef>)

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN task_tag_cross_ref ON tags.id = task_tag_cross_ref.tagId
        WHERE task_tag_cross_ref.taskId = :taskId
        ORDER BY tags.createdAt ASC, tags.name ASC
    """)
    suspend fun getTagsForTask(taskId: Long): List<TagEntity>

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN reward_tag_cross_ref ON tags.id = reward_tag_cross_ref.tagId
        WHERE reward_tag_cross_ref.rewardId = :rewardId
        ORDER BY tags.createdAt ASC, tags.name ASC
    """)
    suspend fun getTagsForReward(rewardId: Long): List<TagEntity>

    @Transaction
    suspend fun replaceTaskTagLinks(taskId: Long, tagIds: List<Long>) {
        clearTaskTagLinks(taskId)
        if (tagIds.isNotEmpty()) {
            insertTaskTagLinks(tagIds.map { tagId -> TaskTagCrossRef(taskId = taskId, tagId = tagId) })
        }
    }

    @Transaction
    suspend fun replaceRewardTagLinks(rewardId: Long, tagIds: List<Long>) {
        clearRewardTagLinks(rewardId)
        if (tagIds.isNotEmpty()) {
            insertRewardTagLinks(tagIds.map { tagId -> RewardTagCrossRef(rewardId = rewardId, tagId = tagId) })
        }
    }

    @Query("DELETE FROM daily_task_template_tag_cross_ref WHERE templateId = :templateId")
    suspend fun clearTemplateTagLinks(templateId: Long)

    @Query("DELETE FROM daily_task_template_tag_cross_ref WHERE tagId = :tagId")
    suspend fun clearTemplateLinksForTag(tagId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateTagLinks(links: List<DailyTaskTemplateTagCrossRef>)

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN daily_task_template_tag_cross_ref ON tags.id = daily_task_template_tag_cross_ref.tagId
        WHERE daily_task_template_tag_cross_ref.templateId = :templateId
        ORDER BY tags.createdAt ASC, tags.name ASC
    """)
    suspend fun getTagsForTemplate(templateId: Long): List<TagEntity>

    @Transaction
    suspend fun replaceTemplateTagLinks(templateId: Long, tagIds: List<Long>) {
        clearTemplateTagLinks(templateId)
        if (tagIds.isNotEmpty()) {
            insertTemplateTagLinks(tagIds.map { tagId -> DailyTaskTemplateTagCrossRef(templateId = templateId, tagId = tagId) })
        }
    }

    @Transaction
    suspend fun deleteTagAndLinks(tag: TagEntity) {
        clearLinksForTag(tag.id)
        clearRewardLinksForTag(tag.id)
        clearTemplateLinksForTag(tag.id)
        deleteTag(tag)
    }
}
