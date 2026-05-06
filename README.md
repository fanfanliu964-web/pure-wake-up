# Pure Wake Up

一个干净、无广告的 CSU 课程表 Android 应用。

做这个项目的原因很直接：WakeUp 课程表这个免费软件加入了开屏广告，打开 App 第一眼就是广告。Pure Wake Up 的目标是把课程表重新做回一个朴素工具：打开就是课表，数据只在本地，功能够用但不打扰。

## 功能

- **CSU `.xls` 导入**：选择教务系统导出的课表文件，先预览再确认导入。
- **导入增强**：自动推测第 1 周周一和总周数，识别同名学期差异，支持替换旧学期或作为新学期导入。
- **周视图**：左右滑动切换周次，完整显示周一到周日，当前节次和今日列高亮。
- **同格多课**：单双周或分段课程落在同一格时显示数量角标，点击可展开全部课程。
- **课程管理**：支持多学期切换、删除学期、新增/编辑/删除课程。
- **桌面小组件**：显示今日课程、正在上课或下一节课，点击课程可回到 App 查看详情。
- **深色模式**：跟随系统自动切换。

## 截图

本仓库不提交伪截图。当前环境没有可用 Android 设备或 AVD，真实周视图和导入预览截图需要在真机/模拟器上安装 debug APK 后补充。

## 导入方式

1. 登录 CSU 教务系统。
2. 导出个人课表为 `.xls` 文件。
3. 打开 Pure Wake Up，点击顶部导入按钮。
4. 选择 `.xls` 文件，检查预览、学期起始日期和总周数。
5. 确认导入。如果检测到同名学期，可以选择替换旧学期或保留为新学期。

## 构建

```bash
git clone https://github.com/fanfanliu964-web/pure-wake-up.git
cd pure-wake-up
./gradlew testDebugUnitTest assembleDebug
```

本地 debug APK 生成位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions 会在 `main` push 后运行单元测试、构建 debug APK，并上传 artifact：

```text
pure-wake-up-debug-apk
```

## 技术栈

- Kotlin + Jetpack Compose
- Room 本地数据库
- HorizontalPager 周次滑动
- JExcelApi 解析 CSU `.xls`
- Glance 桌面小组件
- WorkManager 定时刷新小组件
- minSdk 26 / targetSdk 35

## 隐私

Pure Wake Up 不接入广告 SDK，不上传课表，不请求网络。课程数据、学期数据和手动编辑内容都存储在本机 Room 数据库中。

## License

MIT
