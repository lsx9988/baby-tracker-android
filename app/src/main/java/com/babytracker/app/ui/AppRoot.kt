package com.babytracker.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babytracker.app.ui.health.HealthEntryScreen
import com.babytracker.app.ui.home.AddBabyScreen
import com.babytracker.app.ui.home.HomeScreen
import com.babytracker.app.ui.home.HomeViewModel
import com.babytracker.app.ui.record.FeedEntryScreen

private enum class Screen { HOME, ADD_BABY, ADD_FEED, ADD_HEALTH }

@Composable
fun AppRoot() {
    val viewModel: HomeViewModel = viewModel()
    var screen by mutableStateOf(Screen.HOME)

    when (screen) {
        Screen.HOME -> HomeScreen(
            viewModel = viewModel,
            onAddBaby = { screen = Screen.ADD_BABY },
            onAddFeed = { screen = Screen.ADD_FEED },
            onAddHealth = { screen = Screen.ADD_HEALTH }
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
    }
}
