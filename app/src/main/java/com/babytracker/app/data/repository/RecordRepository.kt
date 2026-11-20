package com.babytracker.app.data.repository

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
import com.babytracker.app.domain.model.CareType
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.domain.model.HealthType
import com.babytracker.app.domain.model.MedicalType
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

/** 记录种类，删除时用于定位具体表 */
enum class RecordKind { FEED, HEALTH, CARE, MEDICAL }

class RecordRepository(
    private val babyDao: BabyDao,
    private val feedDao: FeedDao,
    private val healthDao: HealthDao,
    private val careDao: CareDao,
    private val medicalDao: MedicalDao
) {

    fun feedRecords(babyId: Long): Flow<List<FeedEntity>> = feedDao.observeRecent(babyId)

    fun healthRecords(babyId: Long): Flow<List<HealthEntity>> = healthDao.observeRecent(babyId)

    fun careRecords(babyId: Long): Flow<List<CareEntity>> = careDao.observeRecent(babyId)

    fun medicalRecords(babyId: Long): Flow<List<MedicalEntity>> = medicalDao.observeRecent(babyId)

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

    suspend fun addCare(
        babyId: Long,
        type: CareType,
        detail: String,
        note: String,
        startAt: Long? = null,
        endAt: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    ) = careDao.insert(
        CareEntity(
            babyId = babyId,
            type = type.name,
            detail = detail.trim(),
            note = note.trim(),
            startAt = startAt,
            endAt = endAt,
            timestamp = timestamp
        )
    )

    suspend fun addMedical(
        babyId: Long,
        type: MedicalType,
        title: String,
        dose: String,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ) = medicalDao.insert(
        MedicalEntity(
            babyId = babyId,
            type = type.name,
            title = title.trim(),
            dose = dose.trim(),
            note = note.trim(),
            timestamp = timestamp
        )
    )

    suspend fun deleteById(kind: RecordKind, id: Long) {
        when (kind) {
            RecordKind.FEED -> feedDao.deleteById(id)
            RecordKind.HEALTH -> healthDao.deleteById(id)
            RecordKind.CARE -> careDao.deleteById(id)
            RecordKind.MEDICAL -> medicalDao.deleteById(id)
        }
    }

    // ---- 编辑：按 id 读取 ----
    suspend fun getFeed(id: Long): FeedEntity? = feedDao.getById(id)
    suspend fun getHealth(id: Long): HealthEntity? = healthDao.getById(id)
    suspend fun getCare(id: Long): CareEntity? = careDao.getById(id)
    suspend fun getMedical(id: Long): MedicalEntity? = medicalDao.getById(id)

    // ---- 编辑：更新 ----
    suspend fun updateFeed(record: FeedEntity) = feedDao.update(record)
    suspend fun updateHealth(record: HealthEntity) = healthDao.update(record)
    suspend fun updateCare(record: CareEntity) = careDao.update(record)
    suspend fun updateMedical(record: MedicalEntity) = medicalDao.update(record)

    /** 导出单宝宝全部记录为 JSON 文本 */
    suspend fun exportJson(babyId: Long): String {
        val baby = babyDao.getById(babyId)
        val root = JSONObject()
        root.put("app", "baby-tracker-android")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("baby", JSONObject().apply {
            put("name", baby?.name ?: "")
            put("birthdayMillis", baby?.birthdayMillis ?: 0L)
            put("gender", baby?.gender ?: "")
            put("note", baby?.note ?: "")
        })

        root.put("feed", JSONArray().apply {
            feedDao.getAll(babyId).forEach { r ->
                put(JSONObject().apply {
                    put("type", r.type)
                    put("amount", r.amount ?: JSONObject.NULL)
                    put("durationMin", r.durationMin ?: JSONObject.NULL)
                    put("note", r.note)
                    put("timestamp", r.timestamp)
                })
            }
        })

        root.put("health", JSONArray().apply {
            healthDao.getAll(babyId).forEach { r ->
                put(JSONObject().apply {
                    put("type", r.type)
                    put("value", r.value)
                    put("unit", r.unit)
                    put("note", r.note)
                    put("timestamp", r.timestamp)
                })
            }
        })

        root.put("care", JSONArray().apply {
            careDao.getAll(babyId).forEach { r ->
                put(JSONObject().apply {
                    put("type", r.type)
                    put("detail", r.detail)
                    put("note", r.note)
                    put("startAt", r.startAt ?: JSONObject.NULL)
                    put("endAt", r.endAt ?: JSONObject.NULL)
                    put("timestamp", r.timestamp)
                })
            }
        })

        root.put("medical", JSONArray().apply {
            medicalDao.getAll(babyId).forEach { r ->
                put(JSONObject().apply {
                    put("type", r.type)
                    put("title", r.title)
                    put("dose", r.dose)
                    put("note", r.note)
                    put("timestamp", r.timestamp)
                })
            }
        })

        return root.toString(2)
    }

    /** 导出全部宝宝记录（用于每日自动备份） */
    suspend fun exportAllJson(): String {
        val babies = babyDao.getAll()
        val root = JSONObject()
        root.put("app", "baby-tracker-android")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("babies", JSONArray().apply {
            babies.forEach { baby ->
                put(JSONObject(exportJson(baby.id)))
            }
        })
        return root.toString(2)
    }

    /** 导入 JSON 文本，返回导入成功的记录条数；同名宝宝复用，否则新建 */
    suspend fun importJson(text: String): Int {
        if (text.isBlank()) return 0
        val root = JSONObject(text)

        // 全量备份格式：babies 为数组，逐个导入
        val multi = root.optJSONArray("babies")
        if (multi != null) {
            var total = 0
            for (i in 0 until multi.length()) {
                total += importJson(multi.getJSONObject(i).toString())
            }
            return total
        }

        val babyObj = root.optJSONObject("baby") ?: return 0
        val name = babyObj.optString("name").ifBlank { "导入宝宝" }
        val birthday = babyObj.optLong("birthdayMillis", System.currentTimeMillis())

        val existing = babyDao.getByName(name)
        val babyId = existing?.id ?: babyDao.insert(
            BabyEntity(
                name = name,
                birthdayMillis = birthday,
                gender = babyObj.optString("gender").ifBlank { "UNKNOWN" },
                note = babyObj.optString("note")
            )
        )

        var count = 0

        val feedArr = root.optJSONArray("feed") ?: JSONArray()
        for (i in 0 until feedArr.length()) {
            val o = feedArr.getJSONObject(i)
            val amount = if (o.isNull("amount")) null else o.optDouble("amount")
            val duration = if (o.isNull("durationMin")) null else o.optInt("durationMin")
            addFeed(
                babyId = babyId,
                type = FeedType.fromName(o.optString("type")),
                amount = amount,
                durationMin = duration,
                note = o.optString("note"),
                timestamp = o.optLong("timestamp", System.currentTimeMillis())
            )
            count++
        }

        val healthArr = root.optJSONArray("health") ?: JSONArray()
        for (i in 0 until healthArr.length()) {
            val o = healthArr.getJSONObject(i)
            val type = HealthType.fromName(o.optString("type"))
            addHealth(
                babyId = babyId,
                type = type,
                value = o.optDouble("value", 0.0),
                note = o.optString("note"),
                timestamp = o.optLong("timestamp", System.currentTimeMillis())
            )
            count++
        }

        val careArr = root.optJSONArray("care") ?: JSONArray()
        for (i in 0 until careArr.length()) {
            val o = careArr.getJSONObject(i)
            addCare(
                babyId = babyId,
                type = CareType.fromName(o.optString("type")),
                detail = o.optString("detail"),
                note = o.optString("note"),
                startAt = if (o.isNull("startAt")) null else o.optLong("startAt"),
                endAt = if (o.isNull("endAt")) null else o.optLong("endAt"),
                timestamp = o.optLong("timestamp", System.currentTimeMillis())
            )
            count++
        }

        val medArr = root.optJSONArray("medical") ?: JSONArray()
        for (i in 0 until medArr.length()) {
            val o = medArr.getJSONObject(i)
            addMedical(
                babyId = babyId,
                type = MedicalType.fromName(o.optString("type")),
                title = o.optString("title"),
                dose = o.optString("dose"),
                note = o.optString("note"),
                timestamp = o.optLong("timestamp", System.currentTimeMillis())
            )
            count++
        }

        return count
    }
}
