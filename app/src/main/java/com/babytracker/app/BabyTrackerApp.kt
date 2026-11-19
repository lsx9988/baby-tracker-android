package com.babytracker.app

import android.app.Application
import com.babytracker.app.data.backup.LocalBackup
import com.babytracker.app.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class BabyTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // 每次启动检查一次，当天已备份则跳过
        appScope.launch {
            LocalBackup(container.recordRepository, File(filesDir, "backups"))
                .backupIfNeeded()
        }
    }
}
