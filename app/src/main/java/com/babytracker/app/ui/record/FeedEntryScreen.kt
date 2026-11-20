package com.babytracker.app.ui.record

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
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.ui.components.BackTopBar
import com.babytracker.app.ui.components.DateTimePickerField
import com.babytracker.app.ui.components.FieldLabel
import com.babytracker.app.ui.components.SaveButton
import com.babytracker.app.ui.components.SectionCard
import com.babytracker.app.ui.components.babyChipColors
import com.babytracker.app.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedEntryScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val babies by viewModel.babyRepo.babies.collectAsStateWithLifecycle(emptyList())
    val babyId = viewModel.selectedBabyId ?: babies.firstOrNull()?.id
    val original = viewModel.editingFeed

    LaunchedEffect(Unit) { viewModel.fetchEdit() }

    var type by remember { mutableStateOf(FeedType.BOTTLE) }
    var amount by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(original) {
        original?.let {
            type = FeedType.fromName(it.type)
            amount = it.amount.display()
            duration = it.durationMin?.toString() ?: ""
            note = it.note
            timestamp = it.timestamp
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
            BackTopBar(if (isEdit) "编辑喂养" else "记喂养", onBack)
            Spacer(Modifier.height(16.dp))

        SectionCard {
            FieldLabel("类型")
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedType.values().forEach { t ->
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
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("分量（${type.unit ?: ""}）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("时长（分钟，可选）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
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
            viewModel.upsertFeed(
                babyId = babyId,
                original = original,
                type = type,
                amount = amount.toDoubleOrNull(),
                durationMin = duration.toIntOrNull(),
                note = note,
                timestamp = timestamp
            )
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

private fun Double?.display(): String =
    this?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
