package com.babytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.babytracker.app.data.local.entity.CareEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareDao {

    @Insert
    suspend fun insert(record: CareEntity): Long

    @Delete
    suspend fun delete(record: CareEntity)

    @Query("DELETE FROM care_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM care_records WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(babyId: Long): Flow<List<CareEntity>>

    @Query("SELECT * FROM care_records WHERE babyId = :babyId ORDER BY timestamp DESC")
    suspend fun getAll(babyId: Long): List<CareEntity>
}
