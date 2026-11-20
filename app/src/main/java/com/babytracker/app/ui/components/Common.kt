@file:OptIn(ExperimentalMaterial3Api::class)

package com.babytracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.babytracker.app.data.repository.RecordKind
import com.babytracker.app.ui.theme.CareColor
import com.babytracker.app.ui.theme.FeedColor
import com.babytracker.app.ui.theme.HealthColor
import com.babytracker.app.ui.theme.MedicalColor
import com.babytracker.app.util.formatDateOnly
import com.babytracker.app.util.formatFull
import java.util.Calendar

fun RecordKind.color(): Color = when (this) {
    RecordKind.FEED -> FeedColor
    RecordKind.HEALTH -> HealthColor
    RecordKind.CARE -> CareColor
    RecordKind.MEDICAL -> MedicalColor
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(title: String, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        }
    )
}

/** 录入内容的分组卡片 */
@Composable
fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/** 全宽圆角保存按钮 */
@Composable
fun SaveButton(text: String, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** 类型 / 性别选项 chip 的暖橘配色 */
@Composable
fun babyChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surface,
    labelColor = MaterialTheme.colorScheme.onSurface,
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
    selectedLabelColor = MaterialTheme.colorScheme.primary
)

/** 顶部标题 + 说明的小分组 */
@Composable
fun FieldLabel(title: String, hint: String? = null) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall)
        if (hint != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 入口卡片：彩色圆底 + 标题 + 副标 */
@Composable
fun EntryTile(
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accent, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(title.take(1), style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 日期 + 时间选择字段（先选日期，再选时刻） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(label: String, millis: Long, onChange: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(0) }
    var pickedDay by remember { mutableStateOf(millis) }
    val initCal = Calendar.getInstance().apply { timeInMillis = millis }
    val initHour = initCal.get(Calendar.HOUR_OF_DAY)
    val initMinute = initCal.get(Calendar.MINUTE)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable {
                pickedDay = millis
                step = 0
                open = true
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(formatFull(millis), style = MaterialTheme.typography.bodyLarge)
        }
    }

    if (open && step == 0) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickedDay = dateState.selectedDateMillis ?: millis
                    step = 1
                }) { Text("下一步") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("取消") }
            }
        ) { DatePicker(state = dateState) }
    }

    if (open && step == 1) {
        val timeState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMinute
        )
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    val result = Calendar.getInstance().apply {
                        timeInMillis = pickedDay
                        set(Calendar.HOUR_OF_DAY, timeState.hour)
                        set(Calendar.MINUTE, timeState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    onChange(result)
                    open = false
                    step = 0
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) { Text("上一步") }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}

/** 纯日期选择字段（用于出生日期等） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, millis: Long, onChange: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(initialSelectedDateMillis = millis)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable { open = true }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(formatDateOnly(millis), style = MaterialTheme.typography.bodyLarge)
        }
    }

    if (open) {
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(state.selectedDateMillis ?: millis)
                    open = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("取消") }
            }
        ) { DatePicker(state = state) }
    }
}
