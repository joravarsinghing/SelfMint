package com.ravaroj.habitcurrency.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ravaroj.habitcurrency.data.local.entity.TaskInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskInstanceDao {

    @Query("""
        SELECT * FROM task_instances
        WHERE date = :date
        ORDER BY displayOrder ASC, createdAt DESC
    """)
    fun getTasksForDate(date: String): Flow<List<TaskInstanceEntity>>

    @Query("""
        SELECT * FROM task_instances
        WHERE date = :date
        ORDER BY displayOrder ASC, createdAt DESC
    """)
    suspend fun getTasksForDateOnce(date: String): List<TaskInstanceEntity>

    @Query("""
        SELECT * FROM task_instances
        WHERE date = :date AND isCompleted = 0 AND isFromDailyTemplate = 0
        ORDER BY displayOrder ASC, createdAt DESC
    """)
    suspend fun getIncompleteOneTimeTasksForDate(date: String): List<TaskInstanceEntity>

    @Query("""
        SELECT COUNT(*) FROM task_instances
        WHERE date = :date AND templateId = :templateId
    """)
    suspend fun countInstancesForTemplateOnDate(templateId: Long, date: String): Int

    @Query("SELECT MAX(displayOrder) FROM task_instances WHERE date = :date")
    suspend fun getMaxDisplayOrder(date: String): Int?

    @Transaction
    suspend fun swapDisplayOrders(task1: TaskInstanceEntity, task2: TaskInstanceEntity) {
        update(task1.copy(displayOrder = task2.displayOrder))
        update(task2.copy(displayOrder = task1.displayOrder))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskInstanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskInstanceEntity>)

    @Update
    suspend fun update(task: TaskInstanceEntity)

    @Delete
    suspend fun delete(task: TaskInstanceEntity)

    @Query("DELETE FROM task_instances WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)

    @Query("""
        SELECT COUNT(*) FROM task_instances
        WHERE date = :date AND isCompleted = 1
    """)
    suspend fun getCompletedCountForDate(date: String): Int

    @Query("""
        SELECT COUNT(*) FROM task_instances
        WHERE date = :date AND isCompleted = 0
    """)
    suspend fun getActiveCountForDate(date: String): Int

    @Query("""
        SELECT COALESCE(SUM(rewardValue * CASE 
            WHEN claimCount > 0 THEN claimCount 
            ELSE CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END 
        END), 0)
        FROM task_instances
        WHERE date = :date AND isCompleted = 1
    """)
    suspend fun getTotalEarnedForDate(date: String): Int

    @Query("DELETE FROM task_instances")
    suspend fun clearAll()
}
