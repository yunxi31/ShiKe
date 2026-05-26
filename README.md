# 时刻 (ShiKe)

时刻是一款专注于个人专注与时间管理的**纯本地、极简主义** Android 应用程序。它将番茄工作法（Pomodoro Timer）与独立闹钟完美结合，剔除了所有冗余的云端同步与网络依赖，提供绝对纯净、快速且尊重隐私的使用体验。

---

## ✨ 核心特性

- 🔒 **本地优先与隐私安全**：完全不需要网络权限，所有任务数据、番茄历史与闹钟配置均加密存储于本地 Room 数据库中。
- 🍅 **极简番茄钟**：支持自定义专注时长、快速开始倒计时，并伴有流畅直观的专注状态切换与锁屏通知。
- ⏰ **独立闹钟管理**：支持创建、编辑与切换周期性闹钟，支持耳麦模式和自定义本地音频文件（`MediaPlayer` 直播）提醒。
- 📜 **每日名言灵感**：首页去除了繁琐的个人中心，替换为精选的“每日名言”。使用日期哈希算法，确保单月内每日名言随机且不重复。
- 🎨 **现代化 UI 设计**：基于 Jetpack Compose 与 Material 3 构建，包含精心设计的动态渐变启动动画（Splash Screen）与精致的色彩搭配。

---

## 🛠️ 技术栈

| 模块 / 组件 | 使用技术 | 说明 |
| :--- | :--- | :--- |
| **UI 框架** | Jetpack Compose (Kotlin) | 现代声明式 UI，支持自适应暗色模式 |
| **依赖注入 (DI)** | Dagger Hilt | 全局解耦与生命周期组件注入 |
| **数据存储** | Room Database | 用于高效存取本地 Task 实体与闹钟配置 |
| **状态流** | Kotlin Coroutines & Flow | 响应式数据流与多线程并发控制 |
| **本地偏好** | DataStore Preferences | 替代 SharedPreferences，异步存取用户偏好 |
| **后台与通知** | AlarmManager & Service | 精准闹钟唤醒与前台计时服务 |
| **音频播放** | Android MediaPlayer | 本地音效的流式低延迟播放 |

---

## 📂 项目结构

```text
app/src/main/java/com/pomodoroalert/
│
├── MainActivity.kt          # 应用单 Activity 入口，集成 Compose 导航
├── PomodoroApplication.kt   # Hilt Application 初始化点
│
├── data/                    # 数据持久层
│   ├── TaskEntity.kt        # 本地任务实体（无网络/同步状态字段）
│   ├── TaskDao.kt           # Room 本地查询
│   ├── TaskRepository.kt    # 本地数据源代理
│   └── UserPreferences.kt   # 基础用户偏好（专注时长、语言、耳麦模式等）
│
├── di/                      # 依赖注入配置
│   └── AppModule.kt         # 本地持久化与 Service 依赖提供
│
├── receiver/                # 广播接收器
│   └── AlarmReceiver.kt     # 接收 AlarmManager 精准定时唤醒
│
├── service/                 # 后台服务
│   └── TimerService.kt      # 番茄钟后台计时服务，直接调用本地播放器
│
└── ui/                      # 展现层
    ├── screens/             # Jetpack Compose 页面组件
    │   ├── SplashScreen.kt  # 动画启动页 ("时刻" 渐变 Logo)
    │   ├── HomeScreen.kt    # 主页 (待办列表 & 每日名言)
    │   ├── FocusScreen.kt   # 专注倒计时页面
    │   ├── SettingsScreen.kt# 应用偏好设置页
    │   └── AlarmScreen.kt   # 闹钟唤醒与滑屏停止页面
    │
    └── viewmodel/           # 页面状态持有者
        ├── HomeViewModel.kt
        ├── FocusViewModel.kt
        └── AlarmListViewModel.kt
```

---

## 🚀 快速开始与编译

### 前提条件
* **Android Studio** Koala / Ladybug (或更高版本)
* **JDK 17** 或更高版本
* **Gradle Build Tool** (使用已配置的 Gradle wrapper)

### 构建步骤
1. 克隆代码至本地：
   ```bash
   git clone <your-repository-url>
   ```
2. 使用 Android Studio 打开该项目根目录。
3. 等待 Gradle Sync 完成。
4. 点击 **Run** 或者使用 Gradle 命令行编译 Debug 包：
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📄 许可证

本项目基于 MIT 许可证开源。
