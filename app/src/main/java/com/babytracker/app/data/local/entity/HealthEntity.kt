package com.babytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.babytracker.app.domain.model.HealthType

@Entity(
    tableName = "health_records",
    foreignKeys = [ForeignKey(
        entity = BabyEntity::class,
        parentColumns = ["id"],
        childColumns = ["babyId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("babyId")]
)
data class HealthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val type: String = HealthType.TEMP.name,
    val value: Double,
    val unit: String = HealthType.TEMP.unit,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
