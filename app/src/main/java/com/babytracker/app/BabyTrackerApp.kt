package com.babytracker.app

import android.app.Application
import com.babytracker.app.di.AppContainer

class BabyTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
