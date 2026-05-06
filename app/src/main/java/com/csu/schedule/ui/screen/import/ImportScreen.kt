package com.csu.schedule.ui.screen.`import`

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.`import`.ImportPreview
import com.csu.schedule.data.`import`.PreviewCourse
import com.csu.schedule.ui.viewmodel.ImportState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    importState: ImportState,
    onBack: () -> Unit,
    onPreview: (Uri) -> Unit,
    onImport: (String, Int, Boolean) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember {
        mutableStateOf(guessDefaultStartDate())
    }
    var totalWeeksText by remember { mutableStateOf("20") }
    var replaceMatching by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedUri = uri
        if (uri != null) {
            onPreview(uri)
        }
    }

    val isLoading = importState is ImportState.Loading
    val preview = (importState as? ImportState.PreviewReady)?.preview
    LaunchedEffect(preview?.semesterName) {
        if (preview != null) {
            startDate = preview.suggestedStartDate
            totalWeeksText = preview.totalWeeks.toString()
            replaceMatching = preview.diffSummary.hasMatch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入课表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "步骤 1: 选择课表文件",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = {
                    launcher.launch(arrayOf("application/vnd.ms-excel", "application/octet-stream"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        preview != null -> "已解析课表文件"
                        selectedUri != null -> "已选择文件"
                        else -> "选择 .xls 文件"
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "步骤 2: 确认学期起始日期",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "第1周的周一日期",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(startDate)
            }

            OutlinedTextField(
                value = totalWeeksText,
                onValueChange = { totalWeeksText = it.filter { ch -> ch.isDigit() }.take(2) },
                label = { Text("总周数") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (preview?.diffSummary?.hasMatch == true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = replaceMatching,
                        onCheckedChange = { replaceMatching = it }
                    )
                    Text(
                        text = if (replaceMatching) "替换同名旧学期" else "作为新学期导入",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            when (importState) {
                is ImportState.Loading -> LoadingRow(importState.message)
                is ImportState.PreviewReady -> PreviewPanel(importState.preview)
                is ImportState.Error -> ErrorMessage(importState.message)
                else -> {}
            }

            Button(
                onClick = { onImport(startDate, (totalWeeksText.toIntOrNull() ?: 20).coerceIn(1, 30), replaceMatching) },
                enabled = preview != null && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("确认导入")
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = isoDateToMillis(startDate)
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = millisToIsoDate(millis)
                        }
                        showDatePicker = false
                    }) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("取消")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun LoadingRow(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.padding(8.dp))
        Text(message)
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun PreviewPanel(preview: ImportPreview) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "预览 · ${preview.semesterName}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "发现 ${preview.courseCount} 条课程安排，${preview.uniqueCourseCount} 门课程",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "建议第1周周一: ${preview.suggestedStartDate} · 总周数: ${preview.totalWeeks}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DiffSummaryRow(preview)
            preview.warnings.forEach { warning ->
                Text(
                    text = warning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            HorizontalDivider()
            preview.courses.take(8).forEach { course ->
                PreviewCourseRow(course)
            }
            if (preview.courses.size > 8) {
                Text(
                    text = "还有 ${preview.courses.size - 8} 条课程安排...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DiffSummaryRow(preview: ImportPreview) {
    val diff = preview.diffSummary
    val text = if (diff.hasMatch) {
        "同名学期: 新增 ${diff.newCount} · 变更 ${diff.changedCount} · 未变 ${diff.unchangedCount} · 移除 ${diff.removedCount}"
    } else {
        "未找到同名旧学期，将创建新学期"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (diff.hasChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PreviewCourseRow(course: PreviewCourse) {
    Column {
        Text(
            text = course.courseName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "周${course.dayOfWeek} 第${course.startSection}-${course.endSection}节"
                + if (course.classroom.isNotBlank()) " · ${course.classroom}" else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun guessDefaultStartDate(): String {
    val now = LocalDate.now()
    val year = now.year
    return if (now.monthValue >= 8) {
        "$year-09-01"
    } else {
        "$year-02-24"
    }
}

private fun isoDateToMillis(date: String): Long? {
    return runCatching {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun millisToIsoDate(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)
}
