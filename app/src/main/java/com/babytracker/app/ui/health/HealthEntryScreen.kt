package com.babytracker.app.ui.health

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
import com.babytracker.app.domain.model.HealthType
import com.babytracker.app.ui.components.BackTopBar
import com.babytracker.app.ui.components.FieldLabel
import com.babytracker.app.ui.components.SaveButton
import com.babytracker.app.ui.components.SectionCard
import com.babytracker.app.ui.components.babyChipColors
import com.babytracker.app.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HealthEntryScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val babies by viewModel.babyRepo.babies.collectAsStateWithLifecycle(emptyList())
    val babyId = viewModel.selectedBabyId ?: babies.firstOrNull()?.id

    var type by remember { mutableStateOf(HealthType.TEMP) }
    var value by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTopBar("记健康", onBack)
        Spacer(Modifier.height(16.dp))

        SectionCard {
            FieldLabel("指标")
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HealthType.values().forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        colors = babyChipColors(),
                        label = { Text(t.label) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("数值（${type.unit}）") },
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

        Spacer(Modifier.weight(1f))
        SaveButton("保存") {
            val v = value.toDoubleOrNull()
            if (babyId == null) {
                viewModel.message("请先添加宝宝档案")
            } else if (v == null) {
                viewModel.message("请填写有效的数值")
            } else {
                viewModel.addHealth(babyId, type, v, note)
                onBack()
            }
        }
    }
}
