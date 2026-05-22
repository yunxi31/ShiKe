package com.pomodoroalert.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pomodoroalert.data.TaskEntity
import com.pomodoroalert.ui.viewmodel.HomeViewModel
import com.pomodoroalert.ui.viewmodel.TodoFilter
import com.pomodoroalert.ui.localization.LocalLocalization
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val rawTasks by viewModel.rawTasks.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val loc = LocalLocalization.current
    
    var selectedTab by remember { mutableStateOf(0) } // 0 = Dashboard, 1 = Tasks List
    var showAddSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = Color(0xFFF7F8FC),
        bottomBar = {
            CustomBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onNavToStats = { navController.navigate("stats") },
                onNavToSettings = { navController.navigate("settings") },
                onNavToAlarm = { navController.navigate("alarm") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                shape = CircleShape,
                containerColor = Color(0xFF6C5DD3),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = loc.addTaskDescription, modifier = Modifier.size(28.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DashboardTab(
                    rawTasks = rawTasks,
                    onViewTasks = { selectedTab = 1 },
                    onStartFocus = { taskId -> navController.navigate("focus?taskId=$taskId") },
                    onToggleStatus = viewModel::toggleTaskStatus,
                    onDelete = viewModel::deleteTask
                )
                1 -> TasksTab(
                    tasks = tasks,
                    selectedDate = selectedDate,
                    currentFilter = currentFilter,
                    onDateSelected = viewModel::setSelectedDate,
                    onFilterSelected = viewModel::setFilter,
                    onToggleStatus = viewModel::toggleTaskStatus,
                    onDelete = viewModel::deleteTask,
                    onStartFocus = { taskId -> navController.navigate("focus?taskId=$taskId") }
                )
            }
        }
    }
    
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            AddTaskSheetContent(
                inputText = inputText,
                onInputChange = viewModel::setInput,
                onAddTask = { name, priority, dueDate, duration ->
                    viewModel.addTask(name, priority, dueDate, duration)
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false }
            )
        }
    }
}

@Composable
private fun CustomBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavToStats: () -> Unit,
    onNavToSettings: () -> Unit,
    onNavToAlarm: () -> Unit
) {
    val loc = LocalLocalization.current
    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home (Tab 0)
            IconButton(onClick = { onTabSelected(0) }) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = loc.homeNav,
                    tint = if (selectedTab == 0) Color(0xFF6C5DD3) else Color(0xFF808191)
                )
            }

            // Tasks / Calendar (Tab 1)
            IconButton(onClick = { onTabSelected(1) }) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = loc.calendarNav,
                    tint = if (selectedTab == 1) Color(0xFF6C5DD3) else Color(0xFF808191)
                )
            }

            // Central Alarm Icon
            IconButton(onClick = onNavToAlarm) {
                Icon(
                    imageVector = Icons.Rounded.Alarm,
                    contentDescription = loc.alarmNav,
                    tint = Color(0xFF808191)
                )
            }

            // Stats (Tab 2 or Navigation to StatsScreen)
            IconButton(onClick = onNavToStats) {
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = loc.statsNav,
                    tint = Color(0xFF808191)
                )
            }

            // Settings (Tab 3 or Navigation to SettingsScreen)
            IconButton(onClick = onNavToSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = loc.settingsNav,
                    tint = Color(0xFF808191)
                )
            }
        }
    }
}

@Composable
private fun DashboardTab(
    rawTasks: List<TaskEntity>,
    onViewTasks: () -> Unit,
    onStartFocus: (String) -> Unit,
    onToggleStatus: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    val loc = LocalLocalization.current
    val todayTasks = remember(rawTasks) {
        rawTasks.filter { isToday(it.createdAt) || (it.dueDate?.let { d -> isToday(d) } ?: false) }
    }
    val completedCount = todayTasks.count { it.status == "已完成" }
    val totalCount = todayTasks.size
    val progressPercentage = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    val inProgressTasks = remember(rawTasks) {
        rawTasks.filter { it.status == "进行中" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            DashboardHeader()
        }

        item {
            ProgressHeroCard(progressPercentage = progressPercentage, onViewTasks = onViewTasks)
        }

        item {
            InProgressSection(inProgressTasks = inProgressTasks, onStartFocus = onStartFocus)
        }

        item {
            TaskGroupsSection(
                rawTasks = rawTasks,
                onStartFocus = onStartFocus,
                onToggleStatus = onToggleStatus,
                onDelete = onDelete
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DashboardHeader() {
    val loc = LocalLocalization.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6C5DD3).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("LV", color = Color(0xFF6C5DD3), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(loc.hello, color = Color(0xFF808191), fontSize = 14.sp)
                Text(
                    text = "Livia Vaccaro",
                    color = Color(0xFF1B1D21),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Box {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = loc.notificationsDescription,
                    tint = Color(0xFF1B1D21),
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF7A8A))
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
            )
        }
    }
}

@Composable
private fun ProgressHeroCard(
    progressPercentage: Int,
    onViewTasks: () -> Unit
) {
    val loc = LocalLocalization.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF8B7CF0), Color(0xFF6C5DD3))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loc.heroTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onViewTasks,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = loc.viewTasks,
                            color = Color(0xFF6C5DD3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progressPercentage / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White,
                        strokeWidth = 8.dp,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Text(
                        text = "$progressPercentage%",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun InProgressSection(
    inProgressTasks: List<TaskEntity>,
    onStartFocus: (String) -> Unit
) {
    val loc = LocalLocalization.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = loc.inProgressTitle(inProgressTasks.size),
                color = Color(0xFF1B1D21),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        if (inProgressTasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loc.noOngoingTasks,
                        color = Color(0xFF808191),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(inProgressTasks) { task ->
                    val (groupLabel, groupColor, groupIcon) = when (task.priority) {
                        3 -> Triple(loc.groupWork, Color(0xFFFF7A8A), Icons.Rounded.Work)
                        2 -> Triple(loc.groupStudy, Color(0xFFFFB547), Icons.Rounded.Book)
                        1 -> Triple(loc.groupPersonal, Color(0xFF3F8CFF), Icons.Rounded.Person)
                        else -> Triple(loc.groupMisc, Color(0xFF6C5DD3), Icons.Rounded.Tag)
                    }

                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .clickable { onStartFocus(task.taskId) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = groupLabel,
                                    color = Color(0xFF808191),
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = groupIcon,
                                    contentDescription = null,
                                    tint = groupColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = task.taskName,
                                color = Color(0xFF1B1D21),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { 0.45f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF6C5DD3),
                                trackColor = Color(0xFFF0EFFC)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskGroupsSection(
    rawTasks: List<TaskEntity>,
    onStartFocus: (String) -> Unit,
    onToggleStatus: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    val loc = LocalLocalization.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = loc.taskGroupsTitle,
            color = Color(0xFF1B1D21),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        val groups = listOf(
            Triple(3, loc.groupWork, Color(0xFFFF7A8A)),
            Triple(2, loc.groupStudy, Color(0xFFFFB547)),
            Triple(1, loc.groupPersonal, Color(0xFF3F8CFF)),
            Triple(0, loc.groupMisc, Color(0xFF6C5DD3))
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            groups.forEach { (priority, name, color) ->
                val groupTasks = rawTasks.filter { it.priority == priority }
                TaskGroupCard(
                    priority = priority,
                    name = name,
                    color = color,
                    groupTasks = groupTasks,
                    onStartFocus = onStartFocus,
                    onToggleStatus = onToggleStatus,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun TaskGroupCard(
    priority: Int,
    name: String,
    color: Color,
    groupTasks: List<TaskEntity>,
    onStartFocus: (String) -> Unit,
    onToggleStatus: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    val loc = LocalLocalization.current
    var expanded by remember { mutableStateOf(false) }
    val total = groupTasks.size
    val completed = groupTasks.count { it.status == "已完成" }
    val percentage = if (total > 0) (completed * 100 / total) else 0

    val icon = when (priority) {
        3 -> Icons.Rounded.Work
        2 -> Icons.Rounded.Book
        1 -> Icons.Rounded.Person
        else -> Icons.Rounded.Tag
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(
                if (total > 0) Modifier.clickable { expanded = !expanded } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = name, color = Color(0xFF1B1D21), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = loc.tasksCount(total), color = Color(0xFF808191), fontSize = 12.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(44.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { percentage / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = color,
                            strokeWidth = 4.dp,
                            trackColor = color.copy(alpha = 0.15f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text(
                            text = "$percentage%",
                            color = Color(0xFF1B1D21),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (total > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF808191),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (expanded && total > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF0EFFC))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupTasks.forEach { task ->
                        GroupTaskItem(
                            task = task,
                            groupColor = color,
                            onStartFocus = { onStartFocus(task.taskId) },
                            onToggleStatus = { isCompleted -> onToggleStatus(task.taskId, isCompleted) },
                            onDelete = { onDelete(task.taskId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupTaskItem(
    task: TaskEntity,
    groupColor: Color,
    onStartFocus: () -> Unit,
    onToggleStatus: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val loc = LocalLocalization.current
    val isCompleted = task.status == "已完成"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onToggleStatus(!isCompleted) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isCompleted) groupColor else Color(0xFF808191),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.taskName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isCompleted) Color(0xFF808191) else Color(0xFF1B1D21),
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = Color(0xFF808191),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val mins = (task.duration / 60_000L).toInt()
                    Text(
                        text = loc.minutesUnit(mins),
                        color = Color(0xFF808191),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isCompleted) {
                IconButton(
                    onClick = onStartFocus,
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF6C5DD3).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = loc.startFocus,
                        tint = Color(0xFF6C5DD3),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = loc.delete,
                    tint = Color(0xFF808191).copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TasksTab(
    tasks: List<TaskEntity>,
    selectedDate: Long,
    currentFilter: TodoFilter,
    onDateSelected: (Long) -> Unit,
    onFilterSelected: (TodoFilter) -> Unit,
    onToggleStatus: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onStartFocus: (String) -> Unit
) {
    val loc = LocalLocalization.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = loc.todayTasksTitle,
                color = Color(0xFF1B1D21),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = loc.notificationsDescription,
                    tint = Color(0xFF1B1D21),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        WeeklyCalendarStrip(selectedDate = selectedDate, onDateSelected = onDateSelected)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TodoFilter.values().forEach { filterVal ->
                val isSelected = currentFilter == filterVal
                val label = when (filterVal) {
                    TodoFilter.ALL -> loc.filterAll
                    TodoFilter.TODAY -> loc.filterTodo
                    TodoFilter.IMPORTANT -> loc.filterInProgress
                    TodoFilter.COMPLETED -> loc.filterCompleted
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFF6C5DD3) else Color(0xFF6C5DD3).copy(alpha = 0.08f)
                        )
                        .clickable { onFilterSelected(filterVal) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color(0xFF6C5DD3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = loc.noTasksForStatus,
                    color = Color(0xFF808191),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks, key = { it.taskId }) { task ->
                    FigmaTaskCard(
                        task = task,
                        onToggleStatus = onToggleStatus,
                        onDelete = onDelete,
                        onStartFocus = onStartFocus
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyCalendarStrip(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val loc = LocalLocalization.current
    val calendarDays = remember {
        val list = mutableListOf<Long>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -3)
        for (i in 0 until 7) {
            list.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        calendarDays.forEach { timestamp ->
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val isSelected = isSameDay(timestamp, selectedDate)

            val monthLabel = loc.months[cal.get(Calendar.MONTH)]
            val dayNumber = cal.get(Calendar.DAY_OF_MONTH).toString()
            val dayOfWeek = loc.weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color(0xFF6C5DD3) else Color.Transparent)
                    .clickable { onDateSelected(timestamp) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthLabel,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF808191),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dayNumber,
                        color = if (isSelected) Color.White else Color(0xFF1B1D21),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dayOfWeek,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF808191),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FigmaTaskCard(
    task: TaskEntity,
    onToggleStatus: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onStartFocus: (String) -> Unit
) {
    val loc = LocalLocalization.current
    val isCompleted = task.status == "已完成"
    val isProgress = task.status == "进行中"

    val (groupLabel, groupColor, groupIcon) = when (task.priority) {
        3 -> Triple(loc.groupWork, Color(0xFFFF7A8A), Icons.Rounded.Work)
        2 -> Triple(loc.groupStudy, Color(0xFFFFB547), Icons.Rounded.Book)
        1 -> Triple(loc.groupPersonal, Color(0xFF3F8CFF), Icons.Rounded.Person)
        else -> Triple(loc.groupMisc, Color(0xFF6C5DD3), Icons.Rounded.Tag)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleStatus(task.taskId, !isCompleted) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = groupLabel,
                    color = Color(0xFF808191),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(groupColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = groupIcon,
                        contentDescription = null,
                        tint = groupColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = task.taskName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) Color(0xFF808191) else Color(0xFF1B1D21),
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = Color(0xFF808191),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val mins = (task.duration / 60_000L).toInt()
                    Text(
                        text = loc.minutesUnit(mins),
                        color = Color(0xFF808191),
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isCompleted) {
                        IconButton(
                            onClick = { onStartFocus(task.taskId) },
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF6C5DD3).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = loc.startFocus,
                                tint = Color(0xFF6C5DD3),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDelete(task.taskId) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = loc.delete,
                            tint = Color(0xFF808191).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    val (badgeText, badgeBg, badgeTextColor) = when {
                        isCompleted -> Triple(loc.statusCompleted, Color(0xFFF3F0FF), Color(0xFF6C5DD3))
                        isProgress -> Triple(loc.statusInProgress, Color(0xFFFFF6E5), Color(0xFFFFB547))
                        else -> Triple(loc.statusTodo, Color(0xFFE4F0FF), Color(0xFF3F8CFF))
                    }
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskSheetContent(
    inputText: String,
    onInputChange: (String) -> Unit,
    onAddTask: (String, Int, Long?, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val loc = LocalLocalization.current
    var selectedGroupPriority by remember { mutableStateOf(3) } // Default Office = 3
    var dueDateOption by remember { mutableStateOf(1) } // Default Today = 1
    var durationOption by remember { mutableStateOf(25) } // Default 25 min

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Text(
            text = loc.addProjectOrTask,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B1D21)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = loc.taskGroupLabel,
            fontSize = 12.sp,
            color = Color(0xFF808191),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val groups = listOf(
                3 to loc.groupWork,
                2 to loc.groupStudy,
                1 to loc.groupPersonal,
                0 to loc.groupMisc
            )
            groups.forEach { (p, label) ->
                val isSelected = selectedGroupPriority == p
                val groupColor = when (p) {
                    3 -> Color(0xFFFF7A8A)
                    2 -> Color(0xFFFFB547)
                    1 -> Color(0xFF3F8CFF)
                    else -> Color(0xFF6C5DD3)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) groupColor else groupColor.copy(alpha = 0.08f)
                        )
                        .clickable { selectedGroupPriority = p }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label.take(2),
                        color = if (isSelected) Color.White else groupColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = loc.taskNameLabel,
            fontSize = 12.sp,
            color = Color(0xFF808191),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            placeholder = { Text(loc.placeholderTaskName, color = Color(0xFF808191).copy(alpha = 0.6f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6C5DD3),
                unfocusedBorderColor = Color(0xFF808191).copy(alpha = 0.3f),
                focusedContainerColor = Color(0xFFF7F8FC),
                unfocusedContainerColor = Color(0xFFF7F8FC)
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = loc.durationLabel,
            fontSize = 12.sp,
            color = Color(0xFF808191),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val durations = listOf(25, 45, 60)
            durations.forEach { mins ->
                val isSelected = durationOption == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFF6C5DD3) else Color(0xFF6C5DD3).copy(alpha = 0.08f)
                        )
                        .clickable { durationOption = mins }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loc.durationMinOption(mins),
                        color = if (isSelected) Color.White else Color(0xFF6C5DD3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = loc.dueDateLabel,
            fontSize = 12.sp,
            color = Color(0xFF808191),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val options = listOf(
                1 to loc.dateToday,
                2 to loc.dateTomorrow,
                0 to loc.dateNone
            )
            options.forEach { (opt, label) ->
                val isSelected = dueDateOption == opt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFF6C5DD3) else Color(0xFF6C5DD3).copy(alpha = 0.08f)
                        )
                        .clickable { dueDateOption = opt }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color(0xFF6C5DD3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val name = inputText.trim()
                if (name.isNotEmpty()) {
                    val cal = Calendar.getInstance()
                    val dueDateMs = when (dueDateOption) {
                        1 -> cal.apply {
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                        }.timeInMillis
                        2 -> cal.apply {
                            add(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                        }.timeInMillis
                        else -> null
                    }
                    onAddTask(name, selectedGroupPriority, dueDateMs, durationOption.toLong())
                    keyboardController?.hide()
                } else {
                    Toast.makeText(context, loc.toastEnterTaskName, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5DD3)),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(loc.addTaskButton, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
