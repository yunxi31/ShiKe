package com.pomodoroalert.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class Language(val code: String, val displayName: String) {
    ZH("zh", "中文"),
    EN("en", "English")
}

class LocalizationStrings(
    // Bottom Bar / General Nav
    val homeNav: String,
    val calendarNav: String,
    val alarmNav: String,
    val statsNav: String,
    val settingsNav: String,
    val addTaskDescription: String,
    
    // HomeScreen Header & Hero
    val hello: String,
    val notificationsDescription: String,
    val heroTitle: String,
    val viewTasks: String,
    
    // In Progress Section
    val inProgressTitle: (Int) -> String,
    val noOngoingTasks: String,
    
    // Task Groups Section
    val taskGroupsTitle: String,
    val groupWork: String,
    val groupStudy: String,
    val groupPersonal: String,
    val groupMisc: String,
    val tasksCount: (Int) -> String,
    
    // Tasks Tab / Calendar
    val todayTasksTitle: String,
    val filterAll: String,
    val filterTodo: String,
    val filterInProgress: String,
    val filterCompleted: String,
    val noTasksForStatus: String,
    
    // Calendar Strip Months
    val months: List<String>,
    val weekDays: List<String>,
    
    // Task Card
    val minutesUnit: (Int) -> String,
    val startFocus: String,
    val delete: String,
    val statusCompleted: String,
    val statusInProgress: String,
    val statusTodo: String,
    
    // Add Task Sheet
    val addProjectOrTask: String,
    val taskGroupLabel: String,
    val taskNameLabel: String,
    val placeholderTaskName: String,
    val durationLabel: String,
    val durationMinOption: (Int) -> String,
    val dueDateLabel: String,
    val dateToday: String,
    val dateTomorrow: String,
    val dateNone: String,
    val addTaskButton: String,
    val toastEnterTaskName: String,
    
    // FocusScreen
    val focusingTitle: String,
    val noTaskSelected: String,
    val abandon: String,
    val complete: String,
    val postponeOption: String,
    
    // SettingsScreen
    val settingsTitle: String,
    val backDescription: String,
    val earphoneModeTitle: String,
    val earphoneModeDesc: String,
    val languageSettingTitle: String,
    val languageSettingDesc: String,
    val appLanguage: String,
    val ringtoneModeTitle: String,
    val ringtoneModeDesc: String,
    val ringtoneModeBuiltIn: String,
    val ringtoneModeLocal: String,
    val selectBuiltInRingtoneTitle: String,
    val ringtoneAlertMp3: String,
    val ringtoneAlertWav: String,
    
    // Background permissions info
    val batteryOptimizationTitle: String,
    val batteryOptimizationDesc: String,
    val batteryOptimizationIgnored: String,
    val batteryOptimizationNotIgnored: String,
    val batteryOptimizationBtn: String,
    val autoStartTitle: String,
    val autoStartDesc: String,
    val autoStartBtn: String,
    val lockScreenPermTitle: String,
    val lockScreenPermDesc: String,
    val lockScreenPermBtn: String,
    val notificationsTitle: String,
    val notificationsDesc: String,
    val notificationsEnabled: String,
    val notificationsDisabled: String,
    val notificationsBtn: String,
    val motivationalQuoteTitle: String,
    val motivationalQuoteDesc: String,
    val motivationalQuotePlaceholder: String,
    
    // StatsScreen
    val statsTitle: String,
    val todayPomodoros: String,
    val completedTasksTitle: String,
    
    // AlarmScreen
    val alarmListTitle: String,
    val addAlarm: String,
    val noAlarmsTitle: String,
    val noAlarmsDesc: String,
    val systemDefault: String,
    val customRingtone: String,
    val selectRingtoneTitle: String,
    val remarkLabel: String,
    val ringtonePrefix: String,
    val addBtn: String,
    val editRemarkTitle: String,
    val saveBtn: String,
    val cancelBtn: String,
    val deleteAlarm: String,
    val newAlarmTitle: String,
    val appTagline: String,
    val lockScreenEnabledTitle: String,
    val lockScreenEnabledDesc: String
)

val ZhStrings = LocalizationStrings(
    homeNav = "首页",
    calendarNav = "任务日历",
    alarmNav = "闹钟",
    statsNav = "统计",
    settingsNav = "设置",
    addTaskDescription = "添加任务",
    
    hello = "你好！",
    notificationsDescription = "通知",
    heroTitle = "今日的待办任务\n快要完成啦！",
    viewTasks = "查看任务",
    
    inProgressTitle = { "进行中 ($it)" },
    noOngoingTasks = "暂无进行中的专注任务，点击下方的 + 开始吧！",
    
    taskGroupsTitle = "任务分组 (4)",
    groupWork = "工作项目",
    groupStudy = "学习规划",
    groupPersonal = "个人事务",
    groupMisc = "杂项清单",
    tasksCount = { "$it 任务" },
    
    todayTasksTitle = "今日任务",
    filterAll = "全部",
    filterTodo = "待办",
    filterInProgress = "进行中",
    filterCompleted = "已完成",
    noTasksForStatus = "今日该状态下暂无待办事项",
    
    months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"),
    weekDays = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六"),
    
    minutesUnit = { "${it}分钟" },
    startFocus = "开始专注",
    delete = "删除",
    statusCompleted = "已完成",
    statusInProgress = "进行中",
    statusTodo = "待开始",
    
    addProjectOrTask = "添加项目 / 任务",
    taskGroupLabel = "任务分组",
    taskNameLabel = "项目 / 任务名称",
    placeholderTaskName = "例如: 市场调研",
    durationLabel = "专注时长",
    durationMinOption = { "${it}分钟" },
    dueDateLabel = "截止日期",
    dateToday = "今天",
    dateTomorrow = "明天",
    dateNone = "无日期",
    addTaskButton = "添加任务",
    toastEnterTaskName = "请输入项目/任务名称",
    
    focusingTitle = "专注中",
    noTaskSelected = "未选择任务",
    abandon = "放弃",
    complete = "完成",
    postponeOption = "+10分",
    
    settingsTitle = "偏好设置",
    backDescription = "返回",
    earphoneModeTitle = "耳机模式",
    earphoneModeDesc = "开启后仅在耳机内播报",
    languageSettingTitle = "语言设置",
    languageSettingDesc = "切换应用语言",
    appLanguage = "应用语言",
    ringtoneModeTitle = "闹钟铃声来源",
    ringtoneModeDesc = "选择使用默认铃声或自定义铃声",
    ringtoneModeBuiltIn = "默认铃声",
    ringtoneModeLocal = "自定义铃声",
    selectBuiltInRingtoneTitle = "选择默认铃声",
    ringtoneAlertMp3 = "经典警报声 (alert.mp3)",
    ringtoneAlertWav = "柔和提示音 (alert.wav)",
    
    batteryOptimizationTitle = "电池优化 (后台唤醒)",
    batteryOptimizationDesc = "为保证后台闹钟准时触发，请允许应用忽略电池优化。如果不允许，系统休眠或后台挂起时闹钟将无法触发。",
    batteryOptimizationIgnored = "已忽略电池优化 (推荐)",
    batteryOptimizationNotIgnored = "未忽略电池优化 (闹钟可能会失效)",
    batteryOptimizationBtn = "去关闭电池优化",
    autoStartTitle = "自启动与后台管理",
    autoStartDesc = "在部分手机 (如 OPPO / 小米 / 华为等) 上，您需要手动允许应用「自启动」和「允许后台运行」，否则系统会强制杀死后台闹钟服务。",
    autoStartBtn = "去设置自启动",
    lockScreenPermTitle = "锁屏显示与后台弹出权限",
    lockScreenPermDesc = "在 OPPO / 一加 / 小米等设备上，锁屏唤醒必须开启「锁屏显示」和「后台弹出界面」权限。请点击下方按钮，进入权限管理并允许应用开启这两项权限。",
    lockScreenPermBtn = "去授权锁屏与后台弹出",
    notificationsTitle = "应用通知权限 (关键)",
    notificationsDesc = "由于后台唤醒与锁屏通知依赖系统通知机制，若关闭此权限，闹钟触发时将无法响铃和弹出锁屏页面！",
    notificationsEnabled = "通知权限已开启 (推荐)",
    notificationsDisabled = "通知权限已关闭 (闹钟将完全失效！)",
    notificationsBtn = "去开启通知权限",
    motivationalQuoteTitle = "自定义锁屏鼓励寄语",
    motivationalQuoteDesc = "设置一句能激励自己、调整心态的寄语，在闹钟响起、锁屏亮起时展示在屏幕上。",
    motivationalQuotePlaceholder = "输入你的鼓励寄语，例如：今天又是充满希望的一天，加油！",
    
    statsTitle = "数据统计",
    todayPomodoros = "今日番茄",
    completedTasksTitle = "完成任务",
    
    alarmListTitle = "闹钟列表",
    addAlarm = "添加闹钟",
    noAlarmsTitle = "还没有闹钟",
    noAlarmsDesc = "点击右上角 + 来添加",
    systemDefault = "系统默认",
    customRingtone = "自定义铃声",
    selectRingtoneTitle = "选择闹钟铃声",
    remarkLabel = "备注 (可选)",
    ringtonePrefix = "铃声: ",
    addBtn = "添加",
    editRemarkTitle = "编辑备注",
    saveBtn = "保存",
    cancelBtn = "取消",
    deleteAlarm = "删除闹钟",
    newAlarmTitle = "新闹钟",
    appTagline = "保持专注，高效生活",
    lockScreenEnabledTitle = "闹钟锁屏",
    lockScreenEnabledDesc = "开启后，响铃时在锁屏界面弹出全屏提醒"
)

val EnStrings = LocalizationStrings(
    homeNav = "Home",
    calendarNav = "Calendar",
    alarmNav = "Alarm",
    statsNav = "Stats",
    settingsNav = "Settings",
    addTaskDescription = "Add Task",
    
    hello = "Hello!",
    notificationsDescription = "Notifications",
    heroTitle = "Today's tasks\nare almost done!",
    viewTasks = "View Tasks",
    
    inProgressTitle = { "In Progress ($it)" },
    noOngoingTasks = "No ongoing tasks. Tap + below to start!",
    
    taskGroupsTitle = "Groups (4)",
    groupWork = "Work",
    groupStudy = "Study",
    groupPersonal = "Personal",
    groupMisc = "Misc",
    tasksCount = { if (it > 1) "$it Tasks" else "$it Task" },
    
    todayTasksTitle = "Today's Tasks",
    filterAll = "All",
    filterTodo = "Todo",
    filterInProgress = "Active",
    filterCompleted = "Done",
    noTasksForStatus = "No tasks for this status today",
    
    months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
    weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
    
    minutesUnit = { if (it > 1) "$it mins" else "$it min" },
    startFocus = "Start Focus",
    delete = "Delete",
    statusCompleted = "Completed",
    statusInProgress = "Active",
    statusTodo = "Todo",
    
    addProjectOrTask = "Add Project / Task",
    taskGroupLabel = "Task Group",
    taskNameLabel = "Project / Task Name",
    placeholderTaskName = "e.g. Market Research",
    durationLabel = "Focus Duration",
    durationMinOption = { "$it mins" },
    dueDateLabel = "Due Date",
    dateToday = "Today",
    dateTomorrow = "Tomorrow",
    dateNone = "No Date",
    addTaskButton = "Add Task",
    toastEnterTaskName = "Please enter task name",
    
    focusingTitle = "Focusing",
    noTaskSelected = "No task selected",
    abandon = "Abandon",
    complete = "Complete",
    postponeOption = "+10m",
    
    settingsTitle = "Settings",
    backDescription = "Back",
    earphoneModeTitle = "Headphone Mode",
    earphoneModeDesc = "Play focus alerts in headphones only when connected",
    languageSettingTitle = "Language Settings",
    languageSettingDesc = "Change application language",
    appLanguage = "App Language",
    ringtoneModeTitle = "Ringtone Source",
    ringtoneModeDesc = "Choose between default or custom ringtones",
    ringtoneModeBuiltIn = "Default Ringtone",
    ringtoneModeLocal = "Custom Ringtone",
    selectBuiltInRingtoneTitle = "Select Default Ringtone",
    ringtoneAlertMp3 = "Classic Alert (alert.mp3)",
    ringtoneAlertWav = "Soft Notification (alert.wav)",
    
    batteryOptimizationTitle = "Battery Optimization (Background Sleep)",
    batteryOptimizationDesc = "To ensure background alarms fire on time, please ignore battery optimization. Otherwise, the system may delay or cancel alarms in deep sleep.",
    batteryOptimizationIgnored = "Battery optimization ignored (Recommended)",
    batteryOptimizationNotIgnored = "Battery optimization active (Alarms may fail)",
    batteryOptimizationBtn = "Disable Battery Optimization",
    autoStartTitle = "Auto-start & Background Management",
    autoStartDesc = "On some devices (OPPO, Xiaomi, Huawei, etc.), you must manually enable 'Auto-start' and 'Allow background running' for alarms to work reliably.",
    autoStartBtn = "Go to Auto-start Settings",
    lockScreenPermTitle = "Lock Screen & Pop-up Permission",
    lockScreenPermDesc = "On OPPO/OnePlus/Xiaomi, waking up lock screen requires 'Show on lock screen' and 'Display pop-up windows' permissions. Click below to grant permissions in settings.",
    lockScreenPermBtn = "Grant Lock Screen Permissions",
    notificationsTitle = "App Notification Permission (Critical)",
    notificationsDesc = "Background alarm wakeups rely heavily on the system notification service. If notification permission is off, alarms will not ring or wake up the screen!",
    notificationsEnabled = "Notifications enabled (Recommended)",
    notificationsDisabled = "Notifications disabled (Alarms will fail!)",
    notificationsBtn = "Enable Notifications",
    motivationalQuoteTitle = "Custom Lock Screen Quote",
    motivationalQuoteDesc = "Set an inspirational message to motivate you when the alarm screen pops up on your lock screen.",
    motivationalQuotePlaceholder = "Enter your message, e.g., Today is a brand new day, go for it!",
    
    statsTitle = "Statistics",
    todayPomodoros = "Today's Pomos",
    completedTasksTitle = "Completed Tasks",
    
    alarmListTitle = "Alarm List",
    addAlarm = "Add Alarm",
    noAlarmsTitle = "No alarms yet",
    noAlarmsDesc = "Tap + in the top right to add",
    systemDefault = "System Default",
    customRingtone = "Custom Ringtone",
    selectRingtoneTitle = "Select Alarm Ringtone",
    remarkLabel = "Remark (Optional)",
    ringtonePrefix = "Ringtone: ",
    addBtn = "Add",
    editRemarkTitle = "Edit Remark",
    saveBtn = "Save",
    cancelBtn = "Cancel",
    deleteAlarm = "Delete Alarm",
    newAlarmTitle = "New Alarm",
    appTagline = "Stay Focused, Live Efficiently",
    lockScreenEnabledTitle = "Lock Screen Alarm",
    lockScreenEnabledDesc = "Show full-screen alert when alarm rings on lock screen"
)

val LocalLocalization = staticCompositionLocalOf { ZhStrings }

@Composable
fun ProvideLocalization(language: String, content: @Composable () -> Unit) {
    val strings = if (language == "en") EnStrings else ZhStrings
    CompositionLocalProvider(LocalLocalization provides strings, content = content)
}
