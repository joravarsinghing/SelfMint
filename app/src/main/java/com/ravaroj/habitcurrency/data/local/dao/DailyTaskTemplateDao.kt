package com.ravaroj.habitcurrency.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ravaroj.habitcurrency.data.local.entity.DailyTaskTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskTemplateDao {

    @Query("""
        SELECT * FROM daily_task_templates
        WHERE isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getActiveTemplates(): Flow<List<DailyTaskTemplateEntity>>

    @Query("""
        SELECT * FROM daily_task_templates
        WHERE isActive = 1
        ORDER BY createdAt DESC
    """)
    suspend fun getActiveTemplatesOnce(): List<DailyTaskTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: DailyTaskTemplateEntity): Long

    @Update
    suspend fun update(template: DailyTaskTemplateEntity)

    @Delete
    suspend fun delete(template: DailyTaskTemplateEntity)

    @Query("DELETE FROM daily_task_templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM daily_task_templates")
    suspend fun clearAll()
}
