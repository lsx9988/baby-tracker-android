package com.babytracker.app.data.repository

import com.babytracker.app.data.local.dao.BabyDao
import com.babytracker.app.data.local.entity.BabyEntity
import com.babytracker.app.domain.model.Gender
import kotlinx.coroutines.flow.Flow

class BabyRepository(private val dao: BabyDao) {

    val babies: Flow<List<BabyEntity>> = dao.observeActive()

    suspend fun add(
        name: String,
        birthdayMillis: Long,
        gender: Gender = Gender.UNKNOWN,
        note: String = ""
    ): Long = dao.insert(
        BabyEntity(
            name = name.trim(),
            birthdayMillis = birthdayMillis,
            gender = gender.name,
            note = note.trim()
        )
    )

    suspend fun update(baby: BabyEntity) = dao.update(baby)

    suspend fun delete(baby: BabyEntity) = dao.delete(baby)

    suspend fun getById(id: Long): BabyEntity? = dao.getById(id)
}
