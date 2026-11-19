package com.babytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.babytracker.app.domain.model.CareType

@Entity(
    tableName = "care_records",
    foreignKeys = [ForeignKey(
        entity = BabyEntity::class,
        parentColumns = ["id"],
        childColumns = ["babyId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("babyId")]
)
data class CareEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val type: String = CareType.DIAPER.name,
    val detail: String = "",
    val note: String = "",
    val startAt: Long? = null,
    val endAt: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
