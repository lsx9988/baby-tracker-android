package com.babytracker.app.data.repository

import com.babytracker.app.data.local.dao.FeedDao
import com.babytracker.app.data.local.dao.HealthDao
import com.babytracker.app.data.local.entity.FeedEntity
import com.babytracker.app.data.local.entity.HealthEntity
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.domain.model.HealthType
import kotlinx.coroutines.flow.Flow

class RecordRepository(
    private val feedDao: FeedDao,
    private val healthDao: HealthDao
) {

    fun feedRecords(babyId: Long): Flow<List<FeedEntity>> = feedDao.observeRecent(babyId)

    fun healthRecords(babyId: Long): Flow<List<HealthEntity>> = healthDao.observeRecent(babyId)

    suspend fun addFeed(
        babyId: Long,
        type: FeedType,
        amount: Double?,
        durationMin: Int?,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ) = feedDao.insert(
        FeedEntity(
            babyId = babyId,
            type = type.name,
            amount = amount,
            durationMin = durationMin,
            note = note.trim(),
            timestamp = timestamp
        )
    )

    suspend fun addHealth(
        babyId: Long,
        type: HealthType,
        value: Double,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ) = healthDao.insert(
        HealthEntity(
            babyId = babyId,
            type = type.name,
            value = value,
            unit = type.unit,
            note = note.trim(),
            timestamp = timestamp
        )
    )
}
