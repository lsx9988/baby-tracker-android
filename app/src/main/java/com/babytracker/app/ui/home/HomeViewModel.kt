package com.babytracker.app.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.app.BabyTrackerApp
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.domain.model.Gender
import com.babytracker.app.domain.model.HealthType
import com.babytracker.app.data.repository.BabyRepository
import com.babytracker.app.data.repository.RecordRepository
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as BabyTrackerApp).container
    val babyRepo: BabyRepository = container.babyRepository
    val recordRepo: RecordRepository = container.recordRepository

    var selectedBabyId by mutableStateOf<Long?>(null)
        private set

    fun selectBaby(id: Long) {
        selectedBabyId = id
    }

    fun feedRecords(babyId: Long) = recordRepo.feedRecords(babyId)

    fun healthRecords(babyId: Long) = recordRepo.healthRecords(babyId)

    fun addBaby(name: String, birthdayMillis: Long, gender: Gender, note: String, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val id = babyRepo.add(name, birthdayMillis, gender, note)
            onDone(id)
        }
    }

    fun addFeed(
        babyId: Long,
        type: FeedType,
        amount: Double?,
        durationMin: Int?,
        note: String
    ) {
        viewModelScope.launch {
            recordRepo.addFeed(babyId, type, amount, durationMin, note)
        }
    }

    fun addHealth(babyId: Long, type: HealthType, value: Double, note: String) {
        viewModelScope.launch {
            recordRepo.addHealth(babyId, type, value, note)
        }
    }
}
