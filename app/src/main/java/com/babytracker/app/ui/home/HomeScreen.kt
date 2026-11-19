package com.babytracker.app.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.babytracker.app.data.repository.RecordKind
import com.babytracker.app.domain.model.CareType
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.domain.model.HealthType
import com.babytracker.app.domain.model.MedicalType
import com.babytracker.app.util.ageLabel
import com.babytracker.app.util.formatFull
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
    onAddMedical: () -> Unit
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
            CenterAlignedTopAppBar(
                title = { Text("育儿成长记录") },
                actions = {
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
            )

            if (babies.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("还没有宝宝档案", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "先添加宝宝，再开始记录喂养、健康、护理、医疗等日常",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = onAddBaby) { Text("添加宝宝") }
                }
            } else {
                BabyChips(
                    babies = babies,
                    selectedId = selected?.id,
                    onSelect = viewModel::selectBaby
                )
                Spacer(Modifier.height(8.dp))
                selected?.let {
                    RecordsPanel(
                        baby = it,
                        viewModel = viewModel,
                        onAddFeed = onAddFeed,
                        onAddHealth = onAddHealth,
                        onAddCare = onAddCare,
                        onAddMedical = onAddMedical
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
            FilterChip(
                selected = baby.id == selectedId,
                onClick = { onSelect(baby.id) },
                label = { Text(baby.name) }
            )
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
    onAddMedical: () -> Unit
) {
    val feeds by viewModel.feedRecords(baby.id).collectAsStateWithLifecycle(emptyList())
    val healths by viewModel.healthRecords(baby.id).collectAsStateWithLifecycle(emptyList())
    val cares by viewModel.careRecords(baby.id).collectAsStateWithLifecycle(emptyList())
    val medicals by viewModel.medicalRecords(baby.id).collectAsStateWithLifecycle(emptyList())

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            "${baby.name} · ${ageLabel(baby.birthdayMillis)}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddFeed) { Text("喂养") }
            OutlinedButton(onClick = onAddHealth) { Text("健康") }
            OutlinedButton(onClick = onAddCare) { Text("护理") }
            OutlinedButton(onClick = onAddMedical) { Text("医疗") }
        }

        Spacer(Modifier.height(12.dp))

        val merged: List<RecordItem> = buildList {
            feeds.forEach {
                add(
                    RecordItem(
                        kind = RecordKind.FEED,
                        id = it.id,
                        timestamp = it.timestamp,
                        title = describeFeed(it.type, it.amount, it.durationMin),
                        note = it.note
                    )
                )
            }
            healths.forEach {
                add(
                    RecordItem(
                        kind = RecordKind.HEALTH,
                        id = it.id,
                        timestamp = it.timestamp,
                        title = describeHealth(it.type, it.value, it.unit),
                        note = it.note
                    )
                )
            }
            cares.forEach {
                add(
                    RecordItem(
                        kind = RecordKind.CARE,
                        id = it.id,
                        timestamp = it.timestamp,
                        title = describeCare(it.type, it.detail),
                        note = it.note
                    )
                )
            }
            medicals.forEach {
                add(
                    RecordItem(
                        kind = RecordKind.MEDICAL,
                        id = it.id,
                        timestamp = it.timestamp,
                        title = describeMedical(it.type, it.title, it.dose),
                        note = it.note
                    )
                )
            }
        }.sortedByDescending { it.timestamp }

        if (merged.isEmpty()) {
            Text(
                "还没有记录，点上面的按钮开始记第一条",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(merged, key = { "${it.kind}-${it.id}" }) { row ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(row.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    formatFull(row.timestamp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (row.note.isNotBlank()) {
                                    Text(row.note, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(
                                onClick = { viewModel.deleteRecord(row.kind, row.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除这条记录")
                            }
                        }
                    }
                }
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
