package com.babytracker.app.di

import android.content.Context
import com.babytracker.app.data.local.AppDatabase
import com.babytracker.app.data.repository.BabyRepository
import com.babytracker.app.data.repository.RecordRepository

/** 手写依赖注入容器，随 Application 生命周期持有 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.get(context)

    val babyRepository: BabyRepository = BabyRepository(database.babyDao())

    val recordRepository: RecordRepository = RecordRepository(
        babyDao = database.babyDao(),
        feedDao = database.feedDao(),
        healthDao = database.healthDao(),
        careDao = database.careDao(),
        medicalDao = database.medicalDao()
    )
}
