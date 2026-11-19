package com.babytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.babytracker.app.domain.model.FeedType

@Entity(
    tableName = "feed_records",
    foreignKeys = [ForeignKey(
        entity = BabyEntity::class,
        parentColumns = ["id"],
        childColumns = ["babyId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("babyId")]
)
data class FeedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val type: String = FeedType.BOTTLE.name,
    val amount: Double? = null,
    val durationMin: Int? = null,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
