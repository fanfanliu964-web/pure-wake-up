package com.csu.schedule.ui.screen.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csu.schedule.ui.viewmodel.CourseEditorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditScreen(
    editor: CourseEditorState,
    onBack: () -> Unit,
    onChange: (CourseEditorState) -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editor.id == null) "新增课程" else "编辑课程") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = editor.courseName,
                onValueChange = { onChange(editor.copy(courseName = it)) },
                label = { Text("课程名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberField(
                    value = editor.dayOfWeek,
                    label = "星期",
                    modifier = Modifier.weight(1f),
                    onValueChange = { onChange(editor.copy(dayOfWeek = it)) }
                )
                NumberField(
                    value = editor.startSection,
                    label = "开始节次",
                    modifier = Modifier.weight(1f),
                    onValueChange = { onChange(editor.copy(startSection = it)) }
                )
                NumberField(
                    value = editor.endSection,
                    label = "结束节次",
                    modifier = Modifier.weight(1f),
                    onValueChange = { onChange(editor.copy(endSection = it)) }
                )
            }
            OutlinedTextField(
                value = editor.weekPattern,
                onValueChange = { onChange(editor.copy(weekPattern = it)) },
                label = { Text("周次") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editor.classroom,
                onValueChange = { onChange(editor.copy(classroom = it)) },
                label = { Text("教室") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = editor.classGroup,
                onValueChange = { onChange(editor.copy(classGroup = it)) },
                label = { Text("班级") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            NumberField(
                value = editor.totalHours,
                label = "学时",
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { onChange(editor.copy(totalHours = it)) }
            )
            if (editor.error != null) {
                Text(
                    text = editor.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onValueChange(text.filter { it.isDigit() }.take(3)) },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier
    )
}
