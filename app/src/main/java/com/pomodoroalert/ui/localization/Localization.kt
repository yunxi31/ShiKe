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
    val appTagline: String
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
    appTagline = "保持专注，高效生活"
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
    appTagline = "Stay Focused, Live Efficiently"
)

val LocalLocalization = staticCompositionLocalOf { ZhStrings }

@Composable
fun ProvideLocalization(language: String, content: @Composable () -> Unit) {
    val strings = if (language == "en") EnStrings else ZhStrings
    CompositionLocalProvider(LocalLocalization provides strings, content = content)
}
