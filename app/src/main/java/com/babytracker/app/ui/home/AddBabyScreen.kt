package com.babytracker.app.ui.home

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
import com.babytracker.app.domain.model.Gender
import com.babytracker.app.ui.components.BackTopBar
import com.babytracker.app.ui.components.FieldLabel
import com.babytracker.app.ui.components.SaveButton
import com.babytracker.app.ui.components.SectionCard
import com.babytracker.app.ui.components.babyChipColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddBabyScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.UNKNOWN) }
    var note by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackTopBar("添加宝宝", onBack)
        Spacer(Modifier.height(16.dp))

        SectionCard {
            FieldLabel("昵称")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("昵称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            FieldLabel("性别")
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.values().forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        colors = babyChipColors(),
                        label = { Text(g.label) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.weight(1f))
        fun save() {
            if (name.isBlank()) {
                viewModel.message("请填写昵称")
                return
            }
            viewModel.addBaby(
                name = name,
                birthdayMillis = System.currentTimeMillis(),
                gender = gender,
                note = note
            ) { onBack() }
        }
        SaveButton("保存", onClick = ::save)
    }
}
