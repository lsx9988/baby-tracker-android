package com.babytracker.app.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.app.BabyTrackerApp
import com.babytracker.app.data.repository.BabyRepository
import com.babytracker.app.data.repository.RecordKind
import com.babytracker.app.data.repository.RecordRepository
import com.babytracker.app.domain.model.CareType
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.domain.model.Gender
import com.babytracker.app.domain.model.HealthType
import com.babytracker.app.domain.model.MedicalType
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as BabyTrackerApp).container
    val babyRepo: BabyRepository = container.babyRepository
    val recordRepo: RecordRepository = container.recordRepository

    var selectedBabyId by mutableStateOf<Long?>(null)
        private set

    var toast by mutableStateOf<String?>(null)
        private set

    fun consumeToast() {
        toast = null
    }

    private fun notify(msg: String) {
        toast = msg
    }

    fun selectBaby(id: Long) {
        selectedBabyId = id
    }

    fun feedRecords(babyId: Long) = recordRepo.feedRecords(babyId)

    fun healthRecords(babyId: Long) = recordRepo.healthRecords(babyId)

    fun careRecords(babyId: Long) = recordRepo.careRecords(babyId)

    fun medicalRecords(babyId: Long) = recordRepo.medicalRecords(babyId)

    fun message(msg: String) {
        notify(msg)
    }

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

    fun addCare(
        babyId: Long,
        type: CareType,
        detail: String,
        note: String,
        startAt: Long? = null,
        endAt: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            recordRepo.addCare(babyId, type, detail, note, startAt, endAt, timestamp)
        }
    }

    fun addMedical(
        babyId: Long,
        type: MedicalType,
        title: String,
        dose: String,
        note: String
    ) {
        viewModelScope.launch {
            recordRepo.addMedical(babyId, type, title, dose, note)
        }
    }

    fun deleteRecord(kind: RecordKind, id: Long) {
        viewModelScope.launch {
            recordRepo.deleteById(kind, id)
            notify("已删除 1 条记录")
        }
    }

    /** 导出当前宝宝记录，回传 JSON 文本 */
    fun exportJson(babyId: Long, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val text = recordRepo.exportJson(babyId)
            onReady(text)
        }
    }

    fun importJson(text: String, onDone: (Int) -> Unit) {
        viewModelScope.launch {
            val n = runCatching { recordRepo.importJson(text) }.getOrElse {
                notify("导入失败：文件格式不正确")
                return@launch
            }
            notify("已导入 $n 条记录")
            onDone(n)
        }
    }
}
