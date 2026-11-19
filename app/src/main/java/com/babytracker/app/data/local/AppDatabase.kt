package com.babytracker.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.babytracker.app.data.local.dao.BabyDao
import com.babytracker.app.data.local.dao.CareDao
import com.babytracker.app.data.local.dao.FeedDao
import com.babytracker.app.data.local.dao.HealthDao
import com.babytracker.app.data.local.dao.MedicalDao
import com.babytracker.app.data.local.entity.BabyEntity
import com.babytracker.app.data.local.entity.CareEntity
import com.babytracker.app.data.local.entity.FeedEntity
import com.babytracker.app.data.local.entity.HealthEntity
import com.babytracker.app.data.local.entity.MedicalEntity

@Database(
    entities = [
        BabyEntity::class,
        FeedEntity::class,
        HealthEntity::class,
        CareEntity::class,
        MedicalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun babyDao(): BabyDao
    abstract fun feedDao(): FeedDao
    abstract fun healthDao(): HealthDao
    abstract fun careDao(): CareDao
    abstract fun medicalDao(): MedicalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "baby_tracker.db"
                ).build().also { INSTANCE = it }
            }
    }
}
