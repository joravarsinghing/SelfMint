package com.ravaroj.habitcurrency.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ravaroj.habitcurrency.data.local.entity.RedemptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RedemptionDao {

    @Query("""
        SELECT * FROM redemptions
        WHERE date = :date
        ORDER BY createdAt DESC
    """)
    fun getRedemptionsForDate(date: String): Flow<List<RedemptionEntity>>

    @Query("""
        SELECT * FROM redemptions
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    fun getRecentRedemptions(limit: Int = 20): Flow<List<RedemptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(redemption: RedemptionEntity): Long

    @Delete
    suspend fun delete(redemption: RedemptionEntity)

    @Query("DELETE FROM redemptions WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)

    @Query("SELECT COALESCE(SUM(cost), 0) FROM redemptions WHERE date = :date")
    suspend fun getTotalSpentForDate(date: String): Int

    @Query("SELECT COALESCE(SUM(cost), 0) FROM redemptions")
    suspend fun getTotalSpentAllTime(): Int

    @Query("DELETE FROM redemptions")
    suspend fun clearAll()
}
