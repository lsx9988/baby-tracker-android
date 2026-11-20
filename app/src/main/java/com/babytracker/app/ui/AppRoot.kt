package com.babytracker.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babytracker.app.data.repository.RecordKind
import com.babytracker.app.ui.care.CareEntryScreen
import com.babytracker.app.ui.health.HealthEntryScreen
import com.babytracker.app.ui.home.AddBabyScreen
import com.babytracker.app.ui.home.HomeScreen
import com.babytracker.app.ui.home.HomeViewModel
import com.babytracker.app.ui.medical.MedicalEntryScreen
import com.babytracker.app.ui.record.FeedEntryScreen

private enum class Screen { HOME, ADD_BABY, ADD_FEED, ADD_HEALTH, ADD_CARE, ADD_MEDICAL }

@Composable
fun AppRoot() {
    val viewModel: HomeViewModel = viewModel()
    var screen by remember { mutableStateOf(Screen.HOME) }

    val onEdit: (RecordKind, Long) -> Unit = { kind, id ->
        viewModel.beginEdit(kind, id)
        screen = when (kind) {
            RecordKind.FEED -> Screen.ADD_FEED
            RecordKind.HEALTH -> Screen.ADD_HEALTH
            RecordKind.CARE -> Screen.ADD_CARE
            RecordKind.MEDICAL -> Screen.ADD_MEDICAL
        }
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            viewModel = viewModel,
            onAddBaby = { screen = Screen.ADD_BABY },
            onAddFeed = { screen = Screen.ADD_FEED },
            onAddHealth = { screen = Screen.ADD_HEALTH },
            onAddCare = { screen = Screen.ADD_CARE },
            onAddMedical = { screen = Screen.ADD_MEDICAL },
            onEdit = onEdit
        )

        Screen.ADD_BABY -> AddBabyScreen(
            viewModel = viewModel,
            onBack = { screen = Screen.HOME }
        )

        Screen.ADD_FEED -> FeedEntryScreen(
            viewModel = viewModel,
            onBack = { screen = Screen.HOME }
        )

        Screen.ADD_HEALTH -> HealthEntryScreen(
            viewModel = viewModel,
            onBack = { screen = Screen.HOME }
        )

        Screen.ADD_CARE -> CareEntryScreen(
            viewModel = viewModel,
            onBack = { screen = Screen.HOME }
        )

        Screen.ADD_MEDICAL -> MedicalEntryScreen(
            viewModel = viewModel,
            onBack = { screen = Screen.HOME }
        )
    }
}
