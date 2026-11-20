package com.babytracker.app.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.app.BabyTrackerApp
import com.babytracker.app.data.local.entity.CareEntity
import com.babytracker.app.data.local.entity.FeedEntity
import com.babytracker.app.data.local.entity.HealthEntity
import com.babytracker.app.data.local.entity.MedicalEntity
import com.babytracker.app.data.repository.BabyRepository
import com.babytracker.app.data.repository.RecordKind
import com.babytracker.app.data.repository.RecordRepository
import com.babytracker.app.domain.model.CareType
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.domain.model.Gender
import com.babytracker.app.domain.model.HealthType
import com.babytracker.app.domain.model.MedicalType
import kotlinx.coroutines.launch

/** 待编辑记录定位信息 */
data class EditTarget(val kind: RecordKind, val id: Long)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as BabyTrackerApp).container
    val babyRepo: BabyRepository = container.babyRepository
    val recordRepo: RecordRepository = container.recordRepository

    var selectedBabyId by mutableStateOf<Long?>(null)
        private set

    var toast by mutableStateOf<String?>(null)
        private set

    // ---- 编辑状态 ----
    var editTarget by mutableStateOf<EditTarget?>(null)
        private set
    var editingFeed by mutableStateOf<FeedEntity?>(null)
        private set
    var editingHealth by mutableStateOf<HealthEntity?>(null)
        private set
    var editingCare by mutableStateOf<CareEntity?>(null)
        private set
    var editingMedical by mutableStateOf<MedicalEntity?>(null)
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
            selectedBabyId = id
            onDone(id)
        }
    }

    // ---- 编辑流程 ----

    /** 记录点击：定位待编辑记录，随后由对应录入页拉取回填 */
    fun beginEdit(kind: RecordKind, id: Long) {
        editTarget = EditTarget(kind, id)
        editingFeed = null
        editingHealth = null
        editingCare = null
        editingMedical = null
    }

    /** 录入页进入后调用，按当前 editTarget 拉取实体 */
    fun fetchEdit() {
        val target = editTarget ?: return
        viewModelScope.launch {
            when (target.kind) {
                RecordKind.FEED -> editingFeed = recordRepo.getFeed(target.id)
                RecordKind.HEALTH -> editingHealth = recordRepo.getHealth(target.id)
                RecordKind.CARE -> editingCare = recordRepo.getCare(target.id)
                RecordKind.MEDICAL -> editingMedical = recordRepo.getMedical(target.id)
            }
        }
    }

    fun clearEdit() {
        editTarget = null
        editingFeed = null
        editingHealth = null
        editingCare = null
        editingMedical = null
    }

    fun upsertFeed(
        babyId: Long?,
        original: FeedEntity?,
        type: FeedType,
        amount: Double?,
        durationMin: Int?,
        note: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val target = editTarget
            val existing = if (target?.kind == RecordKind.FEED) {
                recordRepo.getFeed(target.id) ?: original
            } else original
            if (existing != null) {
                recordRepo.updateFeed(
                    existing.copy(
                        type = type.name,
                        amount = amount,
                        durationMin = durationMin,
                        note = note.trim(),
                        timestamp = timestamp
                    )
                )
            } else {
                recordRepo.addFeed(babyId ?: selectedBabyId ?: return@launch, type, amount, durationMin, note, timestamp)
            }
        }
    }

    fun upsertHealth(
        babyId: Long?,
        original: HealthEntity?,
        type: HealthType,
        value: Double,
        note: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val target = editTarget
            val existing = if (target?.kind == RecordKind.HEALTH) {
                recordRepo.getHealth(target.id) ?: original
            } else original
            if (existing != null) {
                recordRepo.updateHealth(
                    existing.copy(
                        type = type.name,
                        value = value,
                        unit = type.unit,
                        note = note.trim(),
                        timestamp = timestamp
                    )
                )
            } else {
                recordRepo.addHealth(babyId ?: selectedBabyId ?: return@launch, type, value, note, timestamp)
            }
        }
    }

    fun upsertCare(
        babyId: Long?,
        original: CareEntity?,
        type: CareType,
        detail: String,
        note: String,
        startAt: Long?,
        endAt: Long?,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val target = editTarget
            val existing = if (target?.kind == RecordKind.CARE) {
                recordRepo.getCare(target.id) ?: original
            } else original
            if (existing != null) {
                recordRepo.updateCare(
                    existing.copy(
                        type = type.name,
                        detail = detail.trim(),
                        note = note.trim(),
                        startAt = startAt,
                        endAt = endAt,
                        timestamp = timestamp
                    )
                )
            } else {
                recordRepo.addCare(babyId ?: selectedBabyId ?: return@launch, type, detail, note, startAt, endAt, timestamp)
            }
        }
    }

    fun upsertMedical(
        babyId: Long?,
        original: MedicalEntity?,
        type: MedicalType,
        title: String,
        dose: String,
        note: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val target = editTarget
            val existing = if (target?.kind == RecordKind.MEDICAL) {
                recordRepo.getMedical(target.id) ?: original
            } else original
            if (existing != null) {
                recordRepo.updateMedical(
                    existing.copy(
                        type = type.name,
                        title = title.trim(),
                        dose = dose.trim(),
                        note = note.trim(),
                        timestamp = timestamp
                    )
                )
            } else {
                recordRepo.addMedical(babyId ?: selectedBabyId ?: return@launch, type, title, dose, note, timestamp)
            }
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
