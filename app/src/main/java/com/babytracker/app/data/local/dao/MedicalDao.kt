package com.babytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.babytracker.app.data.local.entity.MedicalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalDao {

    @Insert
    suspend fun insert(record: MedicalEntity): Long

    @Update
    suspend fun update(record: MedicalEntity)

    @Delete
    suspend fun delete(record: MedicalEntity)

    @Query("DELETE FROM medical_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM medical_records WHERE id = :id")
    suspend fun getById(id: Long): MedicalEntity?

    @Query("SELECT * FROM medical_records WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(babyId: Long): Flow<List<MedicalEntity>>

    @Query("SELECT * FROM medical_records WHERE babyId = :babyId ORDER BY timestamp DESC")
    suspend fun getAll(babyId: Long): List<MedicalEntity>
}
