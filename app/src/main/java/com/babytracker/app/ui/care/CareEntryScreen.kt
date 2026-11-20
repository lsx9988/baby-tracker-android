package com.babytracker.app.ui.care

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babytracker.app.domain.model.CareType
import com.babytracker.app.ui.components.BackTopBar
import com.babytracker.app.ui.components.DateTimePickerField
import com.babytracker.app.ui.components.FieldLabel
import com.babytracker.app.ui.components.SaveButton
import com.babytracker.app.ui.components.SectionCard
import com.babytracker.app.ui.components.babyChipColors
import com.babytracker.app.ui.home.HomeViewModel

private val diaperKinds = listOf("小便", "大便", "混合")
private val spitKinds = listOf("少量", "中量", "大量")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CareEntryScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val babies by viewModel.babyRepo.babies.collectAsStateWithLifecycle(emptyList())
    val babyId = viewModel.selectedBabyId ?: babies.firstOrNull()?.id
    val original = viewModel.editingCare

    LaunchedEffect(Unit) { viewModel.fetchEdit() }

    var type by remember { mutableStateOf(CareType.DIAPER) }
    var diaperKind by remember { mutableStateOf("小便") }
    var spitKind by remember { mutableStateOf("少量") }
    var sleepMin by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(original) {
        original?.let { o ->
            type = CareType.fromName(o.type)
            note = o.note
            timestamp = o.timestamp
            when (type) {
                CareType.DIAPER -> diaperKind = if (o.detail in diaperKinds) o.detail else "小便"
                CareType.SPIT_UP -> spitKind = if (o.detail in spitKinds) o.detail else "少量"
                CareType.SLEEP -> {
                    val mins = if (o.startAt != null && o.endAt != null)
                        ((o.endAt - o.startAt) / 60_000).toString()
                    else o.detail.filter { it.isDigit() }
                    sleepMin = mins
                }
                else -> Unit
            }
        }
    }

    val isEdit = original != null

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 84.dp)
        ) {
        BackTopBar(if (isEdit) "编辑护理" else "记护理", onBack)
        Spacer(Modifier.height(16.dp))

        SectionCard {
            FieldLabel("类型")
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CareType.values().forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        colors = babyChipColors(),
                        label = { Text(t.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            DateTimePickerField("时间", timestamp) { timestamp = it }
            Spacer(Modifier.height(12.dp))

            when (type) {
                CareType.DIAPER -> {
                    FieldLabel("尿布情况")
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        diaperKinds.forEach { k ->
                            FilterChip(
                                selected = diaperKind == k,
                                onClick = { diaperKind = k },
                                colors = babyChipColors(),
                                label = { Text(k) }
                            )
                        }
                    }
                }

                CareType.SLEEP -> {
                    OutlinedTextField(
                        value = sleepMin,
                        onValueChange = { sleepMin = it },
                        label = { Text("睡了多久（分钟）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                CareType.BATH -> {
                    Text(
                        "记录一次洗澡，可在备注补充水温或时长",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }

                CareType.SPIT_UP -> {
                    FieldLabel("吐奶量")
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        spitKinds.forEach { k ->
                            FilterChip(
                                selected = spitKind == k,
                                onClick = { spitKind = k },
                                colors = babyChipColors(),
                                label = { Text(k) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        }

        fun save() {
            if (babyId == null && original == null) {
                viewModel.message("请先添加宝宝档案")
                return
            }
            if (type == CareType.SLEEP) {
                val min = sleepMin.toIntOrNull()
                if (min == null || min <= 0) {
                    viewModel.message("请填写有效的睡眠分钟数")
                    return
                }
                val end = timestamp
                val start = end - min.toLong() * 60_000L
                viewModel.upsertCare(
                    babyId = babyId,
                    original = original,
                    type = type,
                    detail = "${min}分钟",
                    note = note,
                    startAt = start,
                    endAt = end,
                    timestamp = end
                )
            } else {
                val detail = when (type) {
                    CareType.DIAPER -> diaperKind
                    CareType.SPIT_UP -> spitKind
                    else -> ""
                }
                viewModel.upsertCare(babyId, original, type, detail, note, null, null, timestamp)
            }
            viewModel.clearEdit()
            onBack()
        }
        SaveButton(
            if (isEdit) "保存修改" else "保存",
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            onClick = ::save
        )
    }
}
