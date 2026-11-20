package com.babytracker.app.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babytracker.app.data.local.entity.BabyEntity
import com.babytracker.app.data.local.entity.CareEntity
import com.babytracker.app.data.repository.RecordKind
import com.babytracker.app.domain.model.CareType
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.domain.model.HealthType
import com.babytracker.app.domain.model.MedicalType
import com.babytracker.app.ui.components.EntryTile
import com.babytracker.app.ui.components.color
import com.babytracker.app.util.ageLabel
import com.babytracker.app.util.dayLabel
import com.babytracker.app.util.formatFull
import com.babytracker.app.util.relativeAgo
import com.babytracker.app.util.startOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddBaby: () -> Unit,
    onAddFeed: () -> Unit,
    onAddHealth: () -> Unit,
    onAddCare: () -> Unit,
    onAddMedical: () -> Unit,
    onEdit: (RecordKind, Long) -> Unit
) {
    val babies by viewModel.babyRepo.babies.collectAsStateWithLifecycle(emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val toast = viewModel.toast
    LaunchedEffect(toast) {
        if (toast != null) {
            snackbarHostState.showSnackbar(toast)
            viewModel.consumeToast()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val babyId = viewModel.selectedBabyId
            if (babyId == null) {
                viewModel.message("请先选择宝宝档案")
            } else {
                viewModel.exportJson(babyId) { text ->
                    scope.launch(Dispatchers.IO) {
                        val ok = runCatching {
                            context.contentResolver.openOutputStream(uri)?.use {
                                it.write(text.toByteArray(Charsets.UTF_8))
                            } ?: throw IllegalStateException("无法写入")
                        }.isSuccess
                        withContext(Dispatchers.Main) {
                            viewModel.message(if (ok) "已导出 JSON 文件" else "导出失败，请重试")
                        }
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val text = runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
                withContext(Dispatchers.Main) {
                    if (text.isNullOrBlank()) {
                        viewModel.message("读取文件失败，请确认是导出的 JSON")
                    } else {
                        viewModel.importJson(text) { }
                    }
                }
            }
        }
    }

    LaunchedEffect(babies) {
        if (viewModel.selectedBabyId == null && babies.isNotEmpty()) {
            viewModel.selectBaby(babies.first().id)
        }
    }

    val selected = babies.firstOrNull { it.id == viewModel.selectedBabyId } ?: babies.firstOrNull()

    Box(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("育儿成长记录", style = MaterialTheme.typography.titleLarge)
                }
                TextButton(onClick = {
                    val name = "baby-tracker-" +
                        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) +
                        ".json"
                    exportLauncher.launch(name)
                }) { Text("导出") }
                TextButton(onClick = {
                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }) { Text("导入") }
            }

            if (babies.isEmpty()) {
                EmptyBaby(onAddBaby)
            } else {
                if (babies.size > 1) {
                    BabyChips(
                        babies = babies,
                        selectedId = selected?.id,
                        onSelect = viewModel::selectBaby
                    )
                    Spacer(Modifier.height(12.dp))
                }
                selected?.let {
                    RecordsPanel(
                        baby = it,
                        viewModel = viewModel,
                        onAddFeed = onAddFeed,
                        onAddHealth = onAddHealth,
                        onAddCare = onAddCare,
                        onAddMedical = onAddMedical,
                        onEdit = onEdit
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

@Composable
private fun EmptyBaby(onAddBaby: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("还没有宝宝档案", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "先添加宝宝，再开始记录喂养、健康、护理、医疗等日常",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAddBaby, shape = RoundedCornerShape(16.dp)) { Text("添加宝宝") }
    }
}

@Composable
private fun BabyChips(
    babies: List<BabyEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        babies.forEach { baby ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (baby.id == selectedId)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                TextButton(onClick = { onSelect(baby.id) }) {
                    Text(
                        baby.name,
                        color = if (baby.id == selectedId)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordsPanel(
    baby: BabyEntity,
    viewModel: HomeViewModel,
    onAddFeed: () -> Unit,
    onAddHealth: () -> Unit,
    onAddCare: () -> Unit,
    onAddMedical: () -> Unit,
    onEdit: (RecordKind, Long) -> Unit
) {
    val feeds by viewModel.feedRecords(baby.id).collectAsStateWithLifecycle(emptyList())
    val healths by viewModel.healthRecords(baby.id).collectAsStateWithLifecycle(emptyList())
    val cares by viewModel.careRecords(baby.id).collectAsStateWithLifecycle(emptyList())
    val medicals by viewModel.medicalRecords(baby.id).collectAsStateWithLifecycle(emptyList())

    val now = System.currentTimeMillis()
    val todayStart = startOfDay(now)
    val todayFeeds = feeds.filter { it.timestamp >= todayStart }
    val todayDiapers = cares.filter {
        it.timestamp >= todayStart && CareType.fromName(it.type) == CareType.DIAPER
    }
    val todaySleep = cares.filter {
        it.timestamp >= todayStart && CareType.fromName(it.type) == CareType.SLEEP
    }
    val todayMedical = medicals.filter { it.timestamp >= todayStart }

    val feedMl = todayFeeds
        .filter { FeedType.fromName(it.type) in listOf(FeedType.BOTTLE, FeedType.WATER, FeedType.PUMP) }
        .sumOf { it.amount ?: 0.0 }
    val feedMin = todayFeeds
        .filter { FeedType.fromName(it.type) in listOf(FeedType.BREAST_LEFT, FeedType.BREAST_RIGHT) }
        .sumOf { it.durationMin ?: 0 }
    val sleepMinTotal = todaySleep.sumOf { sleepMinutes(it) }
    val lastFeed = feeds.firstOrNull()?.timestamp

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 问候卡片 + 距上次喂养
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        baby.name.firstOrNull()?.uppercase() ?: "宝",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(baby.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        ageLabel(baby.birthdayMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    lastFeed?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "距上次喂养 ${relativeAgo(it, now)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 今日概览矩阵
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                value = "${todayFeeds.size}",
                label = "喂奶",
                sub = if (feedMl > 0) "${feedMl.toInt()} ml" else if (feedMin > 0) "${feedMin} 分" else "—",
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = "${todayDiapers.size}",
                label = "尿便",
                sub = "次",
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = sleepText(sleepMinTotal),
                label = "睡眠",
                sub = "今日",
                modifier = Modifier.weight(1f)
            )
            StatTile(
                value = "${todayMedical.size}",
                label = "医疗",
                sub = "条",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // 2x2 入口
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EntryTile("喂养", "吃奶 · 喝水 · 辅食", com.babytracker.app.ui.theme.FeedColor,
                Modifier.weight(1f), onAddFeed)
            EntryTile("健康", "体温 · 体重 · 身高", com.babytracker.app.ui.theme.HealthColor,
                Modifier.weight(1f), onAddHealth)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EntryTile("护理", "尿布 · 睡眠 · 洗澡", com.babytracker.app.ui.theme.CareColor,
                Modifier.weight(1f), onAddCare)
            EntryTile("医疗", "补剂 · 用药 · 疫苗", com.babytracker.app.ui.theme.MedicalColor,
                Modifier.weight(1f), onAddMedical)
        }

        Spacer(Modifier.height(16.dp))
        Text("最近记录", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        val merged: List<RecordItem> = buildList {
            feeds.forEach {
                add(RecordItem(RecordKind.FEED, it.id, it.timestamp,
                    describeFeed(it.type, it.amount, it.durationMin), it.note))
            }
            healths.forEach {
                add(RecordItem(RecordKind.HEALTH, it.id, it.timestamp,
                    describeHealth(it.type, it.value, it.unit), it.note))
            }
            cares.forEach {
                add(RecordItem(RecordKind.CARE, it.id, it.timestamp,
                    describeCare(it.type, it.detail), it.note))
            }
            medicals.forEach {
                add(RecordItem(RecordKind.MEDICAL, it.id, it.timestamp,
                    describeMedical(it.type, it.title, it.dose), it.note))
            }
        }.sortedByDescending { it.timestamp }

        if (merged.isEmpty()) {
            Text(
                "还没有记录，点上面的卡片开始记第一条",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val groups = linkedMapOf<String, MutableList<RecordItem>>()
            merged.forEach { groups.getOrPut(dayLabel(it.timestamp, now)) { mutableListOf() }.add(it) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                groups.forEach { (label, items) ->
                    item {
                        Text(
                            label,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(items, key = { "${it.kind}-${it.id}" }) { row ->
                        RecordCard(
                            row = row,
                            onEdit = { onEdit(row.kind, row.id) },
                            onDelete = { viewModel.deleteRecord(row.kind, row.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(sub, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecordCard(
    row: RecordItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable(onClick = onEdit)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(row.kind.color(), RoundedCornerShape(5.dp))
            )
            Column(Modifier.weight(1f)) {
                Text(row.title, style = MaterialTheme.typography.titleSmall)
                Text(formatFull(row.timestamp), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (row.note.isNotBlank()) {
                    Text(row.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "删除这条记录")
            }
        }
    }
}

private data class RecordItem(
    val kind: RecordKind,
    val id: Long,
    val timestamp: Long,
    val title: String,
    val note: String
)

private fun sleepMinutes(care: CareEntity): Long {
    if (care.startAt != null && care.endAt != null) return (care.endAt - care.startAt) / 60_000
    return care.detail.filter { it.isDigit() }.toLongOrNull() ?: 0
}

private fun sleepText(minutes: Long): String {
    if (minutes <= 0) return "0"
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h${m}m" else "${m}m"
}

private fun describeFeed(type: String, amount: Double?, durationMin: Int?): String {
    val t = FeedType.fromName(type)
    return when (t) {
        FeedType.BOTTLE, FeedType.WATER, FeedType.PUMP ->
            "${t.label} ${amount?.toInt() ?: "-"}${t.unit ?: ""}"
        FeedType.BREAST_LEFT, FeedType.BREAST_RIGHT ->
            "${t.label} ${durationMin ?: "-"}${t.unit ?: ""}"
        FeedType.SOLID -> "${t.label} ${amount?.toInt() ?: "-"}${t.unit ?: ""}"
    }
}

private fun describeHealth(type: String, value: Double, unit: String): String {
    val t = HealthType.fromName(type)
    return "${t.label} $value $unit"
}

private fun describeCare(type: String, detail: String): String {
    val t = CareType.fromName(type)
    return if (detail.isBlank()) t.label else "${t.label} · $detail"
}

private fun describeMedical(type: String, title: String, dose: String): String {
    val t = MedicalType.fromName(type)
    val suffix = if (dose.isBlank()) "" else " · $dose"
    return "${t.label} · $title$suffix"
}
