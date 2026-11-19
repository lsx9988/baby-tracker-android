package com.babytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.babytracker.app.data.local.entity.BabyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {

    @Insert
    suspend fun insert(baby: BabyEntity): Long

    @Update
    suspend fun update(baby: BabyEntity)

    @Delete
    suspend fun delete(baby: BabyEntity)

    @Query("SELECT * FROM babies WHERE archived = 0 ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<BabyEntity>>

    @Query("SELECT * FROM babies WHERE id = :id")
    suspend fun getById(id: Long): BabyEntity?

    @Query("SELECT * FROM babies WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): BabyEntity?

    @Query("SELECT * FROM babies ORDER BY createdAt ASC")
    suspend fun getAll(): List<BabyEntity>
}
