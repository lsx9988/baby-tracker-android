package com.babytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.babytracker.app.data.local.entity.HealthEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    @Insert
    suspend fun insert(record: HealthEntity)

    @Update
    suspend fun update(record: HealthEntity)

    @Delete
    suspend fun delete(record: HealthEntity)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM health_records WHERE id = :id")
    suspend fun getById(id: Long): HealthEntity?

    @Query("SELECT * FROM health_records WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(babyId: Long): Flow<List<HealthEntity>>

    @Query("SELECT * FROM health_records WHERE babyId = :babyId ORDER BY timestamp DESC")
    suspend fun getAll(babyId: Long): List<HealthEntity>
}
