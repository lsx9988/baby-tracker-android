package com.babytracker.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.babytracker.app.data.local.entity.BabyEntity
import com.babytracker.app.domain.model.FeedType
import com.babytracker.app.domain.model.HealthType
import com.babytracker.app.util.ageLabel
import com.babytracker.app.util.formatFull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddBaby: () -> Unit,
    onAddFeed: () -> Unit,
    onAddHealth: () -> Unit
) {
    val babies by viewModel.babyRepo.babies.collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(babies) {
        if (viewModel.selectedBabyId == null && babies.isNotEmpty()) {
            viewModel.selectBaby(babies.first().id)
        }
    }

    val selected = babies.firstOrNull { it.id == viewModel.selectedBabyId } ?: babies.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CenterAlignedTopAppBar(title = { Text("育儿成长记录") })

        if (babies.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text("还没有宝宝档案", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Text("先添加宝宝，再开始记录喂养、健康等日常", style = MaterialTheme.typography.bodyMedium)
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
                    onAddHealth = onAddHealth
                )
            }
        }
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
    onAddHealth: () -> Unit
) {
    val feeds by viewModel.feedRecords(baby.id).collectAsStateWithLifecycle(emptyList())
    val healths by viewModel.healthRecords(baby.id).collectAsStateWithLifecycle(emptyList())

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            "${baby.name} · ${ageLabel(baby.birthdayMillis)}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddFeed) { Text("记喂养") }
            OutlinedButton(onClick = onAddHealth) { Text("记健康") }
        }

        Spacer(Modifier.height(12.dp))

        val merged: List<RecordRow> = (feeds.map {
            FeedRow(it.timestamp, describeFeed(it.type, it.amount, it.durationMin))
        } + healths.map {
            HealthRow(it.timestamp, describeHealth(it.type, it.value, it.unit))
        }).sortedByDescending { it.timestamp }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(merged) { row ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(row.title, style = MaterialTheme.typography.titleSmall)
                        Text(formatFull(row.timestamp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private interface RecordRow {
    val timestamp: Long
    val title: String
}

private data class FeedRow(
    override val timestamp: Long,
    override val title: String
) : RecordRow

private data class HealthRow(
    override val timestamp: Long,
    override val title: String
) : RecordRow

private fun describeFeed(type: String, amount: Double?, durationMin: Int?): String {
    val t = FeedType.fromName(type)
    return when (t) {
        FeedType.BOTTLE, FeedType.WATER, FeedType.PUMP -> "${t.label} ${amount?.toInt() ?: "-"}${t.unit ?: ""}"
        FeedType.BREAST_LEFT, FeedType.BREAST_RIGHT -> "${t.label} ${durationMin ?: "-"}${t.unit ?: ""}"
        FeedType.SOLID -> "${t.label} ${amount?.toInt() ?: "-"}${t.unit ?: ""}"
    }
}

private fun describeHealth(type: String, value: Double, unit: String): String {
    val t = HealthType.fromName(type)
    return "${t.label} $value $unit"
}
