package com.csu.schedule.ui.screen.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    semesters: List<SemesterEntity>,
    activeSemester: SemesterEntity?,
    courses: List<CourseEntity>,
    onBack: () -> Unit,
    onActivateSemester: (Long) -> Unit,
    onDeleteSemester: (Long) -> Unit,
    onAddCourse: () -> Unit,
    onEditCourse: (CourseEntity) -> Unit,
    onDeleteCourse: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理课表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            if (activeSemester != null) {
                FloatingActionButton(onClick = onAddCourse) {
                    Icon(Icons.Default.Add, contentDescription = "新增课程")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionTitle("学期")
            }
            if (semesters.isEmpty()) {
                item { EmptyText("还没有导入课表") }
            } else {
                items(semesters, key = { it.id }) { semester ->
                    SemesterRow(
                        semester = semester,
                        isActive = semester.id == activeSemester?.id,
                        onActivate = { onActivateSemester(semester.id) },
                        onDelete = { onDeleteSemester(semester.id) }
                    )
                }
            }

            item {
                SectionTitle("课程")
            }
            if (activeSemester == null || courses.isEmpty()) {
                item { EmptyText("当前学期没有课程") }
            } else {
                items(courses, key = { it.id }) { course ->
                    CourseManageRow(
                        course = course,
                        onEdit = { onEditCourse(course) },
                        onDelete = { onDeleteCourse(course.id) }
                    )
                }
            }
            item {
                Spacer(Modifier.padding(bottom = 72.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(vertical = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SemesterRow(
    semester: SemesterEntity,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onActivate)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = semester.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "第1周 ${semester.startDate} · ${semester.totalWeeks} 周",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isActive) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "当前学期",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除学期")
            }
        }
    }
}

@Composable
private fun CourseManageRow(
    course: CourseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
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
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑课程")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除课程")
            }
        }
    }
}
