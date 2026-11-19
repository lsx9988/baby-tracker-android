package com.babytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.babytracker.app.data.local.entity.HealthEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    @Insert
    suspend fun insert(record: HealthEntity)

    @Delete
    suspend fun delete(record: HealthEntity)

    @Query("SELECT * FROM health_records WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(babyId: Long): Flow<List<HealthEntity>>
}
