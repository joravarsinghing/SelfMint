package com.ravaroj.habitcurrency.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ravaroj.habitcurrency.data.local.entity.RewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Query("""
        SELECT * FROM rewards
        WHERE isActive = 1
        ORDER BY displayOrder ASC, createdAt DESC
    """)
    fun getActiveRewards(): Flow<List<RewardEntity>>

    @Query("""
        SELECT * FROM rewards
        WHERE isActive = 1
        ORDER BY displayOrder ASC, createdAt DESC
    """)
    suspend fun getAllActiveRewardsOnce(): List<RewardEntity>

    @Query("SELECT MAX(displayOrder) FROM rewards")
    suspend fun getMaxDisplayOrder(): Int?

    @Transaction
    suspend fun swapDisplayOrders(reward1: RewardEntity, reward2: RewardEntity) {
        update(reward1.copy(displayOrder = reward2.displayOrder))
        update(reward2.copy(displayOrder = reward1.displayOrder))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reward: RewardEntity): Long

    @Update
    suspend fun update(reward: RewardEntity)

    @Delete
    suspend fun delete(reward: RewardEntity)

    @Query("DELETE FROM rewards WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM rewards")
    suspend fun clearAll()
}
