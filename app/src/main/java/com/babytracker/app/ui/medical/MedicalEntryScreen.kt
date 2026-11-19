package com.babytracker.app.ui.medical

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.babytracker.app.domain.model.MedicalType
import com.babytracker.app.ui.home.HomeViewModel

private val hintFor: Map<MedicalType, String> = mapOf(
    MedicalType.SUPPLEMENT to "如：维生素 D3 / 益生菌，剂量写 IU 或滴数",
    MedicalType.MEDICINE to "如：退热药，务必按医嘱剂量记录",
    MedicalType.VACCINE to "如：乙肝疫苗第 2 剂，可填接种医院",
    MedicalType.CHECKUP to "如：42 天体检，可填医院与结论",
    MedicalType.VISIT to "如：发热就诊，可填诊断与用药"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalEntryScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val babies by viewModel.babyRepo.babies.collectAsStateWithLifecycle(emptyList())
    val babyId = viewModel.selectedBabyId ?: babies.firstOrNull()?.id

    var type by remember { mutableStateOf(MedicalType.SUPPLEMENT) }
    var title by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        CenterAlignedTopAppBar(title = { Text("记医疗") })
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MedicalType.entries.forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = { type = t },
                    label = { Text(t.label) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            hintFor[type] ?: "",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("名称 / 事项") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = dose,
            onValueChange = { dose = it },
            label = { Text("剂量 / 次数（可选）") },
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

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (babyId == null) {
                    viewModel.message("请先添加宝宝档案")
                    return@Button
                }
                if (title.isBlank()) {
                    viewModel.message("请填写名称或事项")
                    return@Button
                }
                viewModel.addMedical(babyId, type, title, dose, note)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("保存") }
    }
}
