package com.babytracker.app.ui.care

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babytracker.app.domain.model.CareType
import com.babytracker.app.ui.components.BackTopBar
import com.babytracker.app.ui.components.FieldLabel
import com.babytracker.app.ui.components.SaveButton
import com.babytracker.app.ui.components.SectionCard
import com.babytracker.app.ui.components.babyChipColors
import com.babytracker.app.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CareEntryScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val babies by viewModel.babyRepo.babies.collectAsStateWithLifecycle(emptyList())
    val babyId = viewModel.selectedBabyId ?: babies.firstOrNull()?.id

    var type by remember { mutableStateOf(CareType.DIAPER) }
    var diaperKind by remember { mutableStateOf("小便") }
    var spitKind by remember { mutableStateOf("少量") }
    var sleepMin by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTopBar("记护理", onBack)
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
            when (type) {
                CareType.DIAPER -> {
                    FieldLabel("尿布情况")
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("小便", "大便", "混合").forEach { k ->
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
                        listOf("少量", "中量", "大量").forEach { k ->
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

        Spacer(Modifier.weight(1f))
        fun save() {
            if (babyId == null) {
                viewModel.message("请先添加宝宝档案")
                return
            }
            if (type == CareType.SLEEP) {
                val min = sleepMin.toIntOrNull()
                if (min == null || min <= 0) {
                    viewModel.message("请填写有效的睡眠分钟数")
                    return
                }
                val now = System.currentTimeMillis()
                viewModel.addCare(
                    babyId = babyId,
                    type = type,
                    detail = "${min}分钟",
                    note = note,
                    startAt = now - min.toLong() * 60_000L,
                    endAt = now,
                    timestamp = now
                )
            } else {
                val detail = when (type) {
                    CareType.DIAPER -> diaperKind
                    CareType.SPIT_UP -> spitKind
                    else -> ""
                }
                viewModel.addCare(babyId, type, detail, note)
            }
            onBack()
        }
        SaveButton("保存", onClick = ::save)
    }
}
