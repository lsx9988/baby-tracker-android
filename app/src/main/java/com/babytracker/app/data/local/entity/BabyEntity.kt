package com.babytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.babytracker.app.domain.model.Gender

@Entity(tableName = "babies")
data class BabyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val birthdayMillis: Long,
    val gender: String = Gender.UNKNOWN.name,
    val note: String = "",
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
