# CSU 课程表 App 设计文档

## 概述

面向中南大学 (CSU) 学生的 Android 课程表应用。支持从教务系统导出的 .xls 文件一键导入课表，以周网格视图展示，并提供桌面小组件显示今日课程。

**目标用户**: CSU 全校学生
**平台**: Android (min API 26)
**技术栈**: Kotlin + Jetpack Compose
**数据存储**: 完全本地 (Room)

---

## 1. 技术选型

### XLS 解析: jxl 2.6.12

- CSU 教务系统导出 `.xls` (BIFF8 格式)，不是 `.xlsx`
- jxl 仅 700KB，Apache POI 需 10MB+
- .xls 格式自 Excel 2003 起冻结，jxl 不再更新不影响使用
- 依赖: `net.sourceforge.jexcelapi:jxl:2.6.12`

### 桌面小组件: Jetpack Glance

- Compose 风格 DSL，与主 UI 一致的心智模型
- 自动生成 RemoteViews，减少 60% 样板代码
- 依赖: `androidx.glance:glance-appwidget:1.1.x`, `androidx.glance:glance-material3:1.1.x`

### 导航: 手动状态切换

- 仅 3 个页面，不需要 Compose Navigation
- sealed interface + mutableStateOf + when 即可

```kotlin
sealed interface Screen {
    data object WeekGrid : Screen
    data object Import : Screen
}
```

---

## 2. 视觉设计

### 布局: 周网格视图

主页面为 7 列 × 6 行网格（周一~周日，1-2节 ~ 11-12节），每门课占据一个或多个格子。

### 配色: 柔和马卡龙色

低饱和度浅色背景 + 深色文字，长时间看不累眼。

```
Blue:     bg=#D4E4FF  text=#2D5DAA
Coral:    bg=#FFDDD4  text=#AA4A2D
Green:    bg=#D4FFD6  text=#2D8A33
Yellow:   bg=#FFF4D4  text=#8A7A2D
Purple:   bg=#E8D4FF  text=#6B2DAA
Teal:     bg=#D4FFF4  text=#2D8A7A
Pink:     bg=#FFD4E8  text=#AA2D6B
Orange:   bg=#FFE8D4  text=#AA6B2D
Sky:      bg=#D4EEFF  text=#2D7AAA
Lavender: bg=#F0D4FF  text=#7A2DAA
```

### 风格

- Apple 风格: 大标题、圆角卡片、微妙阴影
- 字体: 系统 sans-serif (接近 SF Pro)
- 周六日列默认隐藏（无课时），可展开

---

## 3. 数据模型

### CourseEntity

```kotlin
@Entity(tableName = "courses", indices = [Index(value = ["semester_id"])])
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val dayOfWeek: Int,         // 1=周一 .. 7=周日
    val startSection: Int,      // 1, 3, 5, 7, 9, 11
    val endSection: Int,        // 2, 4, 6, 8, 10, 12
    val courseName: String,
    val classroom: String,      // "B座118"
    val classGroup: String,     // "应化2304-05"
    val weekPattern: String,    // "1-6,8周(32学时)"
    val totalHours: Int,
    val colorIndex: Int         // 0-9, 对应色板索引
)
```

### SemesterEntity

```kotlin
@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,           // "2025-2026-2"
    val startDate: String,      // Week 1 Monday ISO date
    val totalWeeks: Int,        // 20
    val isActive: Boolean
)
```

### CSU 作息时间 (硬编码常量)

| 节次 | 时间 |
|------|------|
| 1 | 08:00-08:45 |
| 2 | 08:55-09:40 |
| 3 | 10:00-10:45 |
| 4 | 10:55-11:40 |
| 5 | 14:00-14:45 |
| 6 | 14:55-15:40 |
| 7 | 16:00-16:45 |
| 8 | 16:55-17:40 |
| 9 | 19:00-19:45 |
| 10 | 19:55-20:40 |
| 11 | 20:50-21:35 (估计值，XLS未提供) |
| 12 | 21:45-22:30 (估计值，XLS未提供) |

---

## 4. 页面设计

### WeekGridScreen (主页面)

```
+--------------------------------------------------+
|  我的课表                             [导入]       |  大标题
|  < 第12周 (本周) >                                 |  周选择器
+------+------+------+------+------+------+---------+
| 时间 | 周一 | 周二 | 周三 | 周四 | 周五 | 周六/日  |
+------+------+------+------+------+------+---------+
| 1-2  |██████|      |██████|      |██████|         |
| 08:00| 课程 |      | 课程 |      | 课程 |         |
+------+------+------+------+------+------+---------+
| 3-4  |      |██████|      |██████|      |         |
| ...  |      | 课程 |      | 课程 |      |         |
+------+------+------+------+------+------+---------+
```

- 自定义 Compose `Layout`，按 (day, section) 精确定位
- 每门课唯一 courseName 对应稳定的颜色
- 点击课程卡片弹出 CourseDetailSheet

### ImportScreen (导入流程)

1. SAF 文件选择器 (ACTION_OPEN_DOCUMENT, type: application/vnd.ms-excel)
2. 解析中显示进度条
3. 预览: "发现 28 门课程，学期 2025-2026-2"，课程列表
4. 用户确认学期起始日期（日期选择器，预填推测值）
5. 确认导入 → 写入数据库 → 返回主页

### CourseDetailSheet (课程详情)

ModalBottomSheet，展示:
- 课程名称（大字）
- 教室、周次、班级、上课时间

---

## 5. XLS 解析流水线

```
InputStream (SAF ContentResolver)
    ↓
XlsReader        jxl读取 → RawSchedule(studentName, semesterName, grid[7][6])
    ↓
CellParser        每个格子按 "----" 分隔 → List<ParsedCourse>
    ↓
WeekPatternParser "1-6,8周(32学时)" → Set<Int>{1,2,3,4,5,6,8}
    ↓
CourseMapper      分配 colorIndex → List<CourseEntity>
    ↓
Room 数据库
```

### XLS 行列映射

- Row 0: 标题 "中南大学 姓名 学生课表"
- Row 1: 学期信息
- Row 2: 列标题 (跳过)
- Rows 3-9: 周一~周日
- 注意: 原始 XLS 共 26 列，存在合并单元格。XlsReader 需遍历 Row 2 的列标题来定位 "1－2"、"3－4" 等时间段的实际列索引，不可硬编码列号
- 底部: 校历表（用于推算学期起始日期）

### WeekPatternParser 规则

| 输入 | 输出 |
|------|------|
| `1-16周` | {1,2,...,16} |
| `1-6,8周` | {1,2,3,4,5,6,8} |
| `1-16单周` | {1,3,5,...,15} |
| `2-16双周` | {2,4,6,...,16} |
| `1-6,8-12周` | {1,2,3,4,5,6,8,9,10,11,12} |

算法: 去掉"周"和括号内容 → 检测"单/双"后缀 → 按","分段 → 展开范围 → 奇偶过滤

**此模块必须有全面的单元测试。**

### 错误处理

| 错误 | 处理 |
|------|------|
| 非法 .xls 文件 | 提示"此文件不是有效的课程表" |
| 格式不匹配 | 提示"无法解析此文件，是否为CSU课程表？" |
| 部分数据缺失 | 跳过空课程名，其他字段用空字符串 |
| 周次解析失败 | 默认显示在所有周，附加警告标记 |

---

## 6. 桌面小组件

### 组件

- `TodayScheduleWidget` (GlanceAppWidget): 读取 Room → 过滤今日+本周课程 → 渲染列表
- `TodayScheduleWidgetReceiver` (GlanceAppWidgetReceiver): 生命周期入口
- `WidgetUpdateWorker` (CoroutineWorker): WorkManager 每日午夜刷新

### 更新触发

1. 每日午夜: WorkManager 定时任务
2. 导入后: Repository 写入完成后主动刷新
3. 放置时: Glance 自动触发

### 小组件内容

竖向列表: 标题("今日课程 · 第N周 · 星期X") + 课程卡片(时间、课名、教室) + 空状态("今天没有课")

---

## 7. 项目结构

```
com.csu.schedule/
├── App.kt
├── MainActivity.kt
├── data/
│   ├── db/          AppDatabase, CourseEntity, SemesterEntity, Dao
│   ├── import/      XlsReader, CellParser, WeekPatternParser, CourseMapper
│   └── repository/  ScheduleRepository
├── ui/
│   ├── theme/       Theme, Color (马卡龙色板), Type
│   └── screen/
│       ├── Screen.kt
│       ├── weekgrid/ WeekGridScreen, WeekGrid, CourseCard, WeekSelector, CourseDetailSheet
│       └── import/   ImportScreen
├── widget/          TodayScheduleWidget, Receiver, Content, UpdateWorker
└── util/            TimeSlots, WeekCalculator
```

约 25 个文件。

---

## 8. 依赖清单

```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2025.01.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.9.x")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.x")

// Room
implementation("androidx.room:room-runtime:2.6.x")
implementation("androidx.room:room-ktx:2.6.x")
ksp("androidx.room:room-compiler:2.6.x")

// XLS
implementation("net.sourceforge.jexcelapi:jxl:2.6.12")

// Widget
implementation("androidx.glance:glance-appwidget:1.1.x")
implementation("androidx.glance:glance-material3:1.1.x")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.x")
```

---

## 9. 风险与应对

| 风险 | 应对 |
|------|------|
| jxl 在新 Android 上的兼容性 | 所有 jxl 操作在 Dispatchers.IO 执行，已验证 API 21+ 无问题 |
| 周网格自定义 Layout 复杂度 | 用 Compose Layout + Placeable 精确定位，避免嵌套 Row/Column |
| 学期起始日期无法自动提取 | 导入时让用户确认日期，预填校历推测值 |
