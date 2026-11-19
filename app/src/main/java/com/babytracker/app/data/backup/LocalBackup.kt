package com.babytracker.app.data.backup

import com.babytracker.app.data.repository.RecordRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 每日自动本地备份：同一天只写一次，保留最近 7 份。
 * 备份文件为全量 JSON，存应用私有目录 backups/，不申请外部存储权限。
 */
class LocalBackup(private val repo: RecordRepository, private val dir: File) {

    private fun todayTag(): String =
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

    suspend fun backupIfNeeded(): File? {
        if (!dir.exists()) dir.mkdirs()
        val tag = todayTag()
        val target = File(dir, "backup-$tag.json")
        if (target.exists() && target.length() > 0) return null

        val text = runCatching { repo.exportAllJson() }.getOrNull() ?: return null
        if (text.isBlank()) return null

        return runCatching {
            target.writeText(text, Charsets.UTF_8)
            pruneOld()
            target
        }.getOrNull()
    }

    private fun pruneOld(keep: Int = 7) {
        val files = dir.listFiles { f -> f.isFile && f.name.startsWith("backup-") }
            ?.sortedByDescending { it.name } ?: return
        files.drop(keep).forEach { it.delete() }
    }
}
