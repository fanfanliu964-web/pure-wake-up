# Pure Wake Up

一个干净、无广告的 CSU 课程表 Android 应用。

起因是 WakeUp 课程表这款免费软件加入了开屏广告——打开 App 第一眼就是广告，体验很差。于是自己动手做了这个替代品，功能够用，打开即是课表。

---

## 功能

- **导入课表**：从教务系统下载 `.xls` 文件，一键导入
- **周视图**：左右滑动切换周次，丝滑无卡顿
- **完整 7 天**：周六、周日始终显示
- **当前节次高亮**：实时标记正在进行的课程
- **今日列高亮**：一眼定位今天
- **深色模式**：跟随系统自动切换
- **桌面小组件**：今日课程一览

## 截图

> 待补充

## 技术栈

- Kotlin + Jetpack Compose
- Room 数据库
- HorizontalPager 滑动翻周
- Glance 桌面小组件
- 最低支持 Android 8.0（API 26）

## 使用方法

1. 登录学校教务系统，导出课表为 `.xls` 格式
2. 安装 App，点击右上角导入按钮，选择该文件
3. 课表自动解析展示

## 构建

```bash
git clone https://github.com/fanfanliu964-web/pure-wake-up.git
cd pure-wake-up
./gradlew assembleDebug
```

## License

MIT
