package com.babytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.babytracker.app.data.local.entity.FeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    @Insert
    suspend fun insert(record: FeedEntity)

    @Update
    suspend fun update(record: FeedEntity)

    @Delete
    suspend fun delete(record: FeedEntity)

    @Query("DELETE FROM feed_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM feed_records WHERE id = :id")
    suspend fun getById(id: Long): FeedEntity?

    @Query("SELECT * FROM feed_records WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(babyId: Long): Flow<List<FeedEntity>>

    @Query("SELECT * FROM feed_records WHERE babyId = :babyId ORDER BY timestamp DESC")
    suspend fun getAll(babyId: Long): List<FeedEntity>
}
