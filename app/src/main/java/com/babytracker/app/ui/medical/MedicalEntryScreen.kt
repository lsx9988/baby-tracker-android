package com.babytracker.app.ui.medical

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
import com.babytracker.app.domain.model.MedicalType
import com.babytracker.app.ui.components.BackTopBar
import com.babytracker.app.ui.components.FieldLabel
import com.babytracker.app.ui.components.SaveButton
import com.babytracker.app.ui.components.SectionCard
import com.babytracker.app.ui.components.babyChipColors
import com.babytracker.app.ui.home.HomeViewModel

private val hintFor: Map<MedicalType, String> = mapOf(
    MedicalType.SUPPLEMENT to "如：维生素 D3 / 益生菌，剂量写 IU 或滴数",
    MedicalType.MEDICINE to "如：退热药，务必按医嘱剂量记录",
    MedicalType.VACCINE to "如：乙肝疫苗第 2 剂，可填接种医院",
    MedicalType.CHECKUP to "如：42 天体检，可填医院与结论",
    MedicalType.VISIT to "如：发热就诊，可填诊断与用药"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
        BackTopBar("记医疗", onBack)
        Spacer(Modifier.height(16.dp))

        SectionCard {
            FieldLabel("类型", hintFor[type])
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicalType.values().forEach { t ->
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
        }

        Spacer(Modifier.weight(1f))
        fun save() {
            if (babyId == null) {
                viewModel.message("请先添加宝宝档案")
                return
            }
            if (title.isBlank()) {
                viewModel.message("请填写名称或事项")
                return
            }
            viewModel.addMedical(babyId, type, title, dose, note)
            onBack()
        }
        SaveButton("保存", onClick = ::save)
    }
}
