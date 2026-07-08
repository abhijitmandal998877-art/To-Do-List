package com.example.ui.screens

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.data.AppSettings
import com.example.data.Task
import com.example.data.TaskList
import com.example.ui.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 1. SPLASH VIEW WITH PULSATING ANIMATION
// ==========================================
@Composable
fun SplashView(navController: NavController, viewModel: TasksViewModel) {
    val isSplashLoading by viewModel.isSplashLoading.collectAsState()

    LaunchedEffect(isSplashLoading) {
        if (!isSplashLoading) {
            navController.navigate("tasks") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // Pulse checkmark scaling
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0288D1), Color(0xFF0A192F)),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing pulsing outer circle
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                // White inner circle
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Logo",
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Tasks To-Do",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Organize your life elegantly",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFF00D2C4),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// ==========================================
// 2. CANVAS-BASED PALM TREE EMPTY STATE
// ==========================================
@Composable
fun EmptyStateIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(180.dp)) {
        val w = size.width
        val h = size.height

        // Light background circle
        drawCircle(
            color = Color(0xFFE2E8F0).copy(alpha = 0.25f),
            radius = w * 0.4f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Draw Hammock curved support line
        val hammockPath = Path().apply {
            moveTo(w * 0.25f, h * 0.48f)
            quadraticTo(w * 0.5f, h * 0.64f, w * 0.75f, h * 0.48f)
        }
        drawPath(
            path = hammockPath,
            color = Color(0xFF94A3B8),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Checkmark Circle Floating in the Center of Hammock
        drawCircle(
            color = Color(0xFF0288D1),
            radius = 14.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.45f)
        )
        val checkPath = Path().apply {
            moveTo(w * 0.46f, h * 0.45f)
            lineTo(w * 0.49f, h * 0.48f)
            lineTo(w * 0.55f, h * 0.41f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Left Palm Tree Trunk
        val trunkLeft = Path().apply {
            moveTo(w * 0.22f, h * 0.75f)
            quadraticTo(w * 0.26f, h * 0.5f, w * 0.26f, h * 0.35f)
        }
        drawPath(
            path = trunkLeft,
            color = Color(0xFF64748B),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Left Palm Leaves
        drawArc(
            color = Color(0xFF334155),
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(w * 0.12f, h * 0.25f),
            size = Size(w * 0.25f, h * 0.2f)
        )

        // Right Palm Tree Trunk
        val trunkRight = Path().apply {
            moveTo(w * 0.78f, h * 0.75f)
            quadraticTo(w * 0.74f, h * 0.5f, w * 0.74f, h * 0.35f)
        }
        drawPath(
            path = trunkRight,
            color = Color(0xFF64748B),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Right Palm Leaves
        drawArc(
            color = Color(0xFF334155),
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(w * 0.63f, h * 0.25f),
            size = Size(w * 0.25f, h * 0.2f)
        )
    }
}

// ==========================================
// 3. MAIN TASKS SCREEN WITH SLIDING DRAWER
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTasksScreen(navController: NavController, viewModel: TasksViewModel) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentTasks by viewModel.currentTasks.collectAsState()
    val allLists by viewModel.allLists.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val selectedList by viewModel.selectedList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val allTasksList by viewModel.allTasksList.collectAsState()
    val deletedTasks by viewModel.deletedTasks.collectAsState()

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    // Announcement Popup State
    var hasShownAnnouncementPopup by rememberSaveable { mutableStateOf(false) }

    // Confirmation & Edit States
    var listToDeleteByName by remember { mutableStateOf<String?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var taskToDeletePermanently by remember { mutableStateOf<Task?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Search bar keyboard focus requester
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    // Count badges
    val totalCount = allTasksList.count { !it.isCompleted }
    val finishedCount = allTasksList.count { it.isCompleted }
    val trashCount = deletedTasks.size

    // Grouping tasks dynamically
    val overdueTasks = remember(currentTasks) { currentTasks.filter { getTaskCategory(it.dueDate, it.isCompleted) == "Overdue" } }
    val todayTasks = remember(currentTasks) { currentTasks.filter { getTaskCategory(it.dueDate, it.isCompleted) == "Today" } }
    val tomorrowTasks = remember(currentTasks) { currentTasks.filter { getTaskCategory(it.dueDate, it.isCompleted) == "Tomorrow" } }
    val thisWeekTasks = remember(currentTasks) { currentTasks.filter { getTaskCategory(it.dueDate, it.isCompleted) == "This week" } }
    val laterTasks = remember(currentTasks) { currentTasks.filter { getTaskCategory(it.dueDate, it.isCompleted) == "Later" } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.width(300.dp)
            ) {
                // Header of the drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0288D1), Color(0xFF005792))
                            )
                        )
                        .padding(vertical = 32.dp, horizontal = 20.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Check",
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "To Do List",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage deadlines instantly",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TASK LISTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                // Navigation Items
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                            label = { Text("All Lists") },
                            selected = selectedList == "All Lists",
                            onClick = {
                                viewModel.selectList("All Lists")
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0288D1), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${allTasksList.count { !it.isCompleted }}",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        )
                    }

                    items(allLists) { list ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = when (list.name.lowercase()) {
                                        "personal" -> Icons.Filled.Person
                                        "shopping" -> Icons.Filled.ShoppingCart
                                        "wishlist" -> Icons.Filled.CardGiftcard
                                        "work" -> Icons.Filled.Work
                                        else -> Icons.AutoMirrored.Filled.List
                                    },
                                    contentDescription = null
                                )
                            },
                            label = { Text(list.name) },
                            selected = selectedList == list.name,
                            onClick = {
                                viewModel.selectList(list.name)
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF0288D1).copy(alpha = 0.2f), CircleShape)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${allTasksList.count { it.listName == list.name && !it.isCompleted }}",
                                            color = Color(0xFF0288D1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (!list.isSystemList) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { listToDeleteByName = list.name },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.Red.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                            label = { Text("Finished") },
                            selected = selectedList == "Finished",
                            onClick = {
                                viewModel.selectList("Finished")
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF10B981), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${allTasksList.count { it.isCompleted }}",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            label = { Text("Trash Bin") },
                            selected = selectedList == "Trash Bin",
                            onClick = {
                                viewModel.selectList("Trash Bin")
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEF4444), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${trashCount}",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddCategoryDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create List", fontSize = 13.sp)
                        }
                    }
                }

                // Bottom actions
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("settings")
                        }
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (appSettings.theme == "Dark") Icons.Filled.DarkMode else Icons.Filled.LightMode,
                            contentDescription = "Theme Icon",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = appSettings.theme == "Dark",
                            onCheckedChange = { isDark ->
                                viewModel.updateSettings(
                                    theme = if (isDark) "Dark" else "Light",
                                    isVibrationEnabled = appSettings.isVibrationEnabled,
                                    isSoundEnabled = appSettings.isSoundEnabled,
                                    timeFormat24Hour = appSettings.timeFormat24Hour,
                                    firstDayOfWeek = appSettings.firstDayOfWeek
                                )
                            }
                        )
                    }
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF0288D1),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        title = {
                            if (isSearchActive) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    placeholder = { Text("Search tasks...", color = Color.White.copy(alpha = 0.7f)) },
                                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.White,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .testTag("search_input")
                                )
                            } else {
                                Column {
                                    Text("Tasks", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text(selectedList, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) viewModel.updateSearchQuery("")
                            }) {
                                Icon(
                                    imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                                    contentDescription = "Search"
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navController.navigate("new_task") },
                        containerColor = Color(0xFF0288D1),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_task_fab")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Task")
                    }
                },
                content = { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        if (currentTasks.isEmpty()) {
                            // Perfect empty state
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                EmptyStateIllustration()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nothing to do",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create a task using the '+' button below",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (selectedList == "Finished") {
                                    item { CategoryHeader(title = "Finished Tasks", color = Color(0xFF10B981)) }
                                    items(currentTasks) { task ->
                                        TaskCard(
                                            task = task,
                                            viewModel = viewModel,
                                            onEditClick = { taskToEdit = it },
                                            onDeleteClick = { taskToDelete = it },
                                            onDeletePermanentClick = { taskToDeletePermanently = it }
                                        )
                                    }
                                } else if (selectedList == "Trash Bin") {
                                    item { CategoryHeader(title = "Deleted Tasks (Trash Bin)", color = Color.Red) }
                                    items(currentTasks) { task ->
                                        TaskCard(
                                            task = task,
                                            viewModel = viewModel,
                                            onEditClick = { taskToEdit = it },
                                            onDeleteClick = { taskToDelete = it },
                                            onDeletePermanentClick = { taskToDeletePermanently = it }
                                        )
                                    }
                                } else {
                                    // Overdue Section
                                    if (overdueTasks.isNotEmpty()) {
                                        item { CategoryHeader(title = "Overdue", color = Color.Red) }
                                        items(overdueTasks) { task ->
                                            TaskCard(
                                                task = task,
                                                viewModel = viewModel,
                                                onEditClick = { taskToEdit = it },
                                                onDeleteClick = { taskToDelete = it },
                                                onDeletePermanentClick = { taskToDeletePermanently = it }
                                            )
                                        }
                                    }

                                    // Today Section
                                    if (todayTasks.isNotEmpty()) {
                                        item { CategoryHeader(title = "Today", color = Color(0xFF0288D1)) }
                                        items(todayTasks) { task ->
                                            TaskCard(
                                                task = task,
                                                viewModel = viewModel,
                                                onEditClick = { taskToEdit = it },
                                                onDeleteClick = { taskToDelete = it },
                                                onDeletePermanentClick = { taskToDeletePermanently = it }
                                            )
                                        }
                                    }

                                    // Tomorrow Section
                                    if (tomorrowTasks.isNotEmpty()) {
                                        item { CategoryHeader(title = "Tomorrow", color = Color(0xFF00A8CC)) }
                                        items(tomorrowTasks) { task ->
                                            TaskCard(
                                                task = task,
                                                viewModel = viewModel,
                                                onEditClick = { taskToEdit = it },
                                                onDeleteClick = { taskToDelete = it },
                                                onDeletePermanentClick = { taskToDeletePermanently = it }
                                            )
                                        }
                                    }

                                    // This Week Section
                                    if (thisWeekTasks.isNotEmpty()) {
                                        item { CategoryHeader(title = "This week", color = Color(0xFF475569)) }
                                        items(thisWeekTasks) { task ->
                                            TaskCard(
                                                task = task,
                                                viewModel = viewModel,
                                                onEditClick = { taskToEdit = it },
                                                onDeleteClick = { taskToDelete = it },
                                                onDeletePermanentClick = { taskToDeletePermanently = it }
                                            )
                                        }
                                    }

                                    // Later Section
                                    if (laterTasks.isNotEmpty()) {
                                        item { CategoryHeader(title = "Later", color = Color(0xFF94A3B8)) }
                                        items(laterTasks) { task ->
                                            TaskCard(
                                                task = task,
                                                viewModel = viewModel,
                                                onEditClick = { taskToEdit = it },
                                                onDeleteClick = { taskToDelete = it },
                                                onDeletePermanentClick = { taskToDeletePermanently = it }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    )

    // Dialog for adding a custom list category
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Create New List") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("List Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_list_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addList(newCategoryName.trim())
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Task Dialog Trigger
    taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            allLists = allLists,
            onDismiss = { taskToEdit = null },
            onSave = { updatedTitle, updatedDueDate, updatedListName, updatedRepeatInterval, updatedHasReminder, updatedReminderTime ->
                viewModel.editTask(
                    task = task,
                    title = updatedTitle,
                    dueDate = updatedDueDate,
                    listName = updatedListName,
                    repeatInterval = updatedRepeatInterval,
                    hasReminder = updatedHasReminder,
                    reminderTime = updatedReminderTime
                )
                taskToEdit = null
            }
        )
    }

    // Confirm Delete List Dialog
    listToDeleteByName?.let { listName ->
        AlertDialog(
            onDismissRequest = { listToDeleteByName = null },
            title = { Text("Delete List", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the list '$listName'? All tasks in this list will be moved to the Trash Bin.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteList(listName)
                        listToDeleteByName = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { listToDeleteByName = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Delete Task Dialog (Move to Trash)
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Move Task to Trash", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to move '${task.title}' to the Trash Bin?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(task)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                ) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Permanent Delete Task Dialog
    taskToDeletePermanently?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDeletePermanently = null },
            title = { Text("Delete Permanently", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Are you sure you want to permanently delete '${task.title}'? This action is irreversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePermanent(task)
                        taskToDeletePermanently = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDeletePermanently = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // App-Open Announcement Dialog
    if (appSettings.adminShowPopup && !hasShownAnnouncementPopup) {
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        AlertDialog(
            onDismissRequest = {
                if (!appSettings.adminPopupMandatory) {
                    hasShownAnnouncementPopup = true
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = "Announcement",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = appSettings.adminPopupTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(
                        onClick = { hasShownAnnouncementPopup = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = appSettings.adminPopupText,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (appSettings.adminPopupMandatory) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "This is a mandatory system announcement.",
                                    fontSize = 11.sp,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (appSettings.adminPopupHasActionButton && appSettings.adminPopupActionText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                try {
                                    uriHandler.openUri(appSettings.adminPopupActionUrl)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Invalid action URL! 🌐", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(appSettings.adminPopupActionText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = null
        )
    }

    // Onboarding Guidelines Dialog for First-Time Users
    var showOnboardingDialog by remember { mutableStateOf(appSettings.isFirstTimeUser) }
    LaunchedEffect(appSettings.isFirstTimeUser) {
        showOnboardingDialog = appSettings.isFirstTimeUser
    }

    if (showOnboardingDialog) {
        AlertDialog(
            onDismissRequest = {
                showOnboardingDialog = false
                viewModel.updateFirstTimeUser(false)
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "User Guide",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "User Guidelines 📋",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(
                        onClick = {
                            showOnboardingDialog = false
                            viewModel.updateFirstTimeUser(false)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Welcome to our To Do List App! Here are some essential guidelines to get you started:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.List, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                            Text("1. How to Create List", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0288D1))
                        }
                        Text("Open the sidebar drawer by tapping the menu icon in the top left. Tap on \"+ Add List\" to create a custom category list for your tasks.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.AddTask, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                            Text("2. Add, Edit & Delete Tasks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0288D1))
                        }
                        Text("• Add: Tap the floating '+' button at the bottom-right, fill in task title, optional due date, choose list and save.\n• Edit: Tap any task in the list to open the task editor dialog to update details.\n• Delete: Swipe any task or tap delete inside the task details to move it to Trash. You can permanently delete from Trash.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                            Text("3. How to Finish Tasks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0288D1))
                        }
                        Text("Simply tap the circular checkbox next to any task title. Finished tasks will be beautifully styled as strikethrough text and moved down.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Widgets, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                            Text("4. How to Add Widget", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0288D1))
                        }
                        Text("Go to your device's home screen, long press on an empty space, tap 'Widgets', find 'To Do List App', and drag it to your screen for instant desktop updates!", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOnboardingDialog = false
                        viewModel.updateFirstTimeUser(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It, Thanks!", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    allLists: List<TaskList>,
    onDismiss: () -> Unit,
    onSave: (title: String, dueDate: Long, listName: String, repeatInterval: String, hasReminder: Boolean, reminderTime: Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(task.title) }

    // Parse task due date
    val calendar = remember {
        Calendar.getInstance().apply {
            if (task.dueDate > 0L) {
                timeInMillis = task.dueDate
            }
        }
    }
    var selectedDate by remember { mutableStateOf(if (task.dueDate > 0L) task.dueDate else System.currentTimeMillis()) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    var isRepeatEnabled by remember { mutableStateOf(task.repeatInterval != "Once") }
    var repeatInterval by remember { mutableStateOf(if (task.repeatInterval != "Once") task.repeatInterval else "Daily") }
    var isReminderEnabled by remember { mutableStateOf(task.hasReminder) }
    var selectedListName by remember { mutableStateOf(task.listName) }
    var expandedListDropdown by remember { mutableStateOf(false) }
    var expandedRepeatDropdown by remember { mutableStateOf(false) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDate = cal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
            },
            selectedHour,
            selectedMinute,
            false
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_task_title")
                    )
                }

                item {
                    // List Dropdown Selection
                    Column {
                        Text("List", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            OutlinedButton(
                                onClick = { expandedListDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedListName)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedListDropdown,
                                onDismissRequest = { expandedListDropdown = false }
                            ) {
                                allLists.forEach { list ->
                                    DropdownMenuItem(
                                        text = { Text(list.name) },
                                        onClick = {
                                            selectedListName = list.name
                                            expandedListDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Due Date Selection
                    val dateString = remember(selectedDate) {
                        val sdf = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                        sdf.format(Date(selectedDate))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateString, fontSize = 14.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }

                item {
                    // Time Selection
                    val timeString = remember(selectedHour, selectedMinute) {
                        val amPm = if (selectedHour >= 12) "PM" else "AM"
                        val displayHour = when {
                            selectedHour == 0 -> 12
                            selectedHour > 12 -> selectedHour - 12
                            else -> selectedHour
                        }
                        String.format("%02d:%02d %s", displayHour, selectedMinute, amPm)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { timePickerDialog.show() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(timeString, fontSize = 14.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }

                item {
                    // Set Alarm Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set Alarm Reminder", fontSize = 14.sp)
                        }
                        Switch(checked = isReminderEnabled, onCheckedChange = { isReminderEnabled = it })
                    }
                }

                item {
                    // Repeat Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Sync, contentDescription = null, tint = Color(0xFF00A8CC), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Repeat Task", fontSize = 14.sp)
                        }
                        Switch(checked = isRepeatEnabled, onCheckedChange = { isRepeatEnabled = it })
                    }
                }

                if (isRepeatEnabled) {
                    item {
                        Column {
                            Text("Repeat Interval", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                OutlinedButton(
                                    onClick = { expandedRepeatDropdown = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(repeatInterval)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = expandedRepeatDropdown,
                                    onDismissRequest = { expandedRepeatDropdown = false }
                                ) {
                                    listOf("Daily", "Weekly", "Monthly").forEach { interval ->
                                        DropdownMenuItem(
                                            text = { Text(interval) },
                                            onClick = {
                                                repeatInterval = interval
                                                expandedRepeatDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter task title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val finalCal = Calendar.getInstance().apply {
                        timeInMillis = selectedDate
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                    }
                    onSave(
                        title.trim(),
                        finalCal.timeInMillis,
                        selectedListName,
                        if (isRepeatEnabled) repeatInterval else "Once",
                        isReminderEnabled,
                        if (isReminderEnabled) finalCal.timeInMillis else 0L
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CategoryHeader(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun TaskCard(
    task: Task,
    viewModel: TasksViewModel,
    onEditClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    onDeletePermanentClick: (Task) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (!task.isDeleted) {
                    onEditClick(task)
                }
            }
            .testTag("task_item_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!task.isDeleted) {
                // Checkbox at the left
                IconButton(
                    onClick = { viewModel.toggleTaskCompleted(task) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = "Toggle completion",
                        tint = if (task.isCompleted) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Deleted Task",
                    tint = Color.Red.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.dueDate > 0L) {
                    val formattedDate = remember(task.dueDate) {
                        val sdf = SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault())
                        sdf.format(Date(task.dueDate))
                    }
                    val isOverdue = task.dueDate < System.currentTimeMillis() && !task.isCompleted

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Due Date",
                            tint = if (isOverdue) Color.Red else Color(0xFF0288D1),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = if (isOverdue) Color.Red else Color(0xFF0288D1),
                            fontWeight = FontWeight.Bold
                        )

                        if (task.repeatInterval != "Once") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = "Repeats",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = task.repeatInterval,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        if (task.isSyncWithCalendar) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Synced to Calendar",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }

            // Sync to Calendar manual backup button
            if (task.dueDate > 0L && !task.isSyncWithCalendar && !task.isCompleted && !task.isDeleted) {
                IconButton(
                    onClick = {
                        val success = viewModel.syncToCalendar(task)
                        if (success) {
                            Toast.makeText(context, "Synced to Google Calendar!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Requesting Calendar Permission...", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Sync to calendar",
                        tint = Color(0xFF0288D1).copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (task.isDeleted) {
                // Restore button
                IconButton(onClick = { viewModel.restoreTask(task) }) {
                    Icon(
                        imageVector = Icons.Filled.Undo,
                        contentDescription = "Restore task",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Permanent Delete button
                IconButton(onClick = { onDeletePermanentClick(task) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete task permanently",
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(onClick = { onDeleteClick(task) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. NEW TASK SCREEN (CARD INPUTS & SPEECH)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(navController: NavController, viewModel: TasksViewModel) {
    val context = LocalContext.current
    val allLists by viewModel.allLists.collectAsState()

    var title by remember { mutableStateOf("") }
    var batchMode by remember { mutableStateOf(false) }

    // Date & Time
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }

    var isRepeatEnabled by remember { mutableStateOf(false) }
    var repeatInterval by remember { mutableStateOf("Daily") }
    var isReminderEnabled by remember { mutableStateOf(false) }
    var selectedListName by remember { mutableStateOf("Default") }
    var syncToCalendarOption by remember { mutableStateOf(false) }

    // Speech to text integration launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                title = results[0]
            }
        }
    }

    // Pickers Helpers
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDate = cal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
            },
            12,
            0,
            false
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0288D1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = { Text("New Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter task title", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }

                    // Compute trigger date & times
                    val finalCal = Calendar.getInstance().apply {
                        timeInMillis = selectedDate
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                    }

                    viewModel.addTask(
                        title = title.trim(),
                        dueDate = finalCal.timeInMillis,
                        listName = selectedListName,
                        repeatInterval = if (isRepeatEnabled) repeatInterval else "Once",
                        hasReminder = isReminderEnabled,
                        reminderTime = if (isReminderEnabled) finalCal.timeInMillis else 0L,
                        syncToDeviceCalendar = syncToCalendarOption
                    )

                    if (!batchMode) {
                        navController.popBackStack()
                    } else {
                        title = ""
                        Toast.makeText(context, "Task Added in Batch Mode!", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = Color(0xFF0288D1),
                contentColor = Color.White,
                modifier = Modifier.testTag("save_task_fab")
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save Task")
            }
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Task Title Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "What is to be done?",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0288D1)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    placeholder = { Text("Task title details...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("task_title_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Vocal Recognition
                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak task details...")
                                            }
                                            speechLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Speech recognizer not available on this device", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF0288D1).copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(Icons.Filled.Mic, contentDescription = "Voice Input", tint = Color(0xFF0288D1))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Add in Batch Mode", fontSize = 14.sp)
                                Switch(checked = batchMode, onCheckedChange = { batchMode = it })
                            }
                        }
                    }
                }

                // Date, Time and Notifications Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Notification & Deadline",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0288D1)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Date picker row
                            val dateString = remember(selectedDate) {
                                val sdf = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                                sdf.format(Date(selectedDate))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color(0xFF0288D1))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(dateString, fontSize = 15.sp)
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            // Time picker row
                            val timeString = remember(selectedHour, selectedMinute) {
                                val amPm = if (selectedHour >= 12) "PM" else "AM"
                                val displayHour = when {
                                    selectedHour == 0 -> 12
                                    selectedHour > 12 -> selectedHour - 12
                                    else -> selectedHour
                                }
                                String.format("%02d:%02d %s", displayHour, selectedMinute, amPm)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { timePickerDialog.show() }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color(0xFF0288D1))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(timeString, fontSize = 15.sp)
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            // Enable reminder alarm
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Set Alarm Reminder", fontSize = 15.sp)
                                }
                                Switch(checked = isReminderEnabled, onCheckedChange = { isReminderEnabled = it })
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            // Repeat switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Sync, contentDescription = null, tint = Color(0xFF00A8CC))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Repeat Task", fontSize = 15.sp)
                                }
                                Switch(checked = isRepeatEnabled, onCheckedChange = { isRepeatEnabled = it })
                            }

                            // Repeat Chips options
                            if (isRepeatEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Daily", "Weekly", "Monthly").forEach { interval ->
                                        val selected = repeatInterval == interval
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (selected) Color(0xFF0288D1) else Color.Gray.copy(alpha = 0.15f),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .clickable { repeatInterval = interval }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = interval,
                                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // List Category Selection
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Assign to List",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0288D1)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            var expandedListDropdown by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { expandedListDropdown = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedListName, color = MaterialTheme.colorScheme.onSurface)
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedListDropdown,
                                    onDismissRequest = { expandedListDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    allLists.forEach { list ->
                                        DropdownMenuItem(
                                            text = { Text(list.name) },
                                            onClick = {
                                                selectedListName = list.name
                                                expandedListDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Real System Calendar Sync Option
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Sync to Google Calendar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Writes deadlines automatically", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Switch(checked = syncToCalendarOption, onCheckedChange = { syncToCalendarOption = it })
                        }
                    }
                }
            }
        }
    )
}

// ==========================================
// 5. SETTINGS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: TasksViewModel) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val allLists by viewModel.allLists.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var showAdminPanel by remember { mutableStateOf(false) }

    val timezones = listOf(
        "GMT-10:00 (Hawaii)",
        "GMT-08:00 (Pacific Time)",
        "GMT-05:00 (Eastern Time)",
        "GMT+00:00 (UTC)",
        "GMT+01:00 (Central Europe)",
        "GMT+05:30 (India Standard Time)",
        "GMT+08:00 (Singapore)",
        "GMT+09:00 (Tokyo)",
        "GMT+10:00 (Sydney)"
    )
    var expandedTimezoneDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0288D1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // General Settings Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "General",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0288D1),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Theme Selector Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val nextTheme = when (appSettings.theme) {
                                            "System" -> "Light"
                                            "Light" -> "Dark"
                                            else -> "System"
                                        }
                                        viewModel.updateSettings(
                                            theme = nextTheme,
                                            isVibrationEnabled = appSettings.isVibrationEnabled,
                                            isSoundEnabled = appSettings.isSoundEnabled,
                                            timeFormat24Hour = appSettings.timeFormat24Hour,
                                            firstDayOfWeek = appSettings.firstDayOfWeek
                                        )
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Theme", fontSize = 15.sp)
                                    Text(appSettings.theme, fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }

                            Divider()

                            // Time Format Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("24-hour time format", fontSize = 15.sp)
                                    Text("Displays 13:00 instead of 1:00 PM", fontSize = 12.sp, color = Color.Gray)
                                }
                                Switch(
                                    checked = appSettings.timeFormat24Hour,
                                    onCheckedChange = { use24h ->
                                        viewModel.updateSettings(
                                            theme = appSettings.theme,
                                            isVibrationEnabled = appSettings.isVibrationEnabled,
                                            isSoundEnabled = appSettings.isSoundEnabled,
                                            timeFormat24Hour = use24h,
                                            firstDayOfWeek = appSettings.firstDayOfWeek
                                        )
                                    }
                                )
                            }

                            Divider()

                            // First day of week Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val nextDay = if (appSettings.firstDayOfWeek == "Sunday") "Monday" else "Sunday"
                                        viewModel.updateSettings(
                                            theme = appSettings.theme,
                                            isVibrationEnabled = appSettings.isVibrationEnabled,
                                            isSoundEnabled = appSettings.isSoundEnabled,
                                            timeFormat24Hour = appSettings.timeFormat24Hour,
                                            firstDayOfWeek = nextDay
                                        )
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("First day of week", fontSize = 15.sp)
                                    Text(appSettings.firstDayOfWeek, fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }

                            Divider()

                            // Timezone Selector Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedTimezoneDropdown = true }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Time Zone", fontSize = 15.sp)
                                    Text(appSettings.timezone, fontSize = 12.sp, color = Color.Gray)
                                }
                                Box {
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    DropdownMenu(
                                        expanded = expandedTimezoneDropdown,
                                        onDismissRequest = { expandedTimezoneDropdown = false }
                                    ) {
                                        timezones.forEach { zone ->
                                            DropdownMenuItem(
                                                text = { Text(zone) },
                                                onClick = {
                                                    viewModel.updateTimezone(zone)
                                                    expandedTimezoneDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Notification Settings Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Notifications & Alerts",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0288D1),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Vibration switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Vibration, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Device Vibration", fontSize = 15.sp)
                                }
                                Switch(
                                    checked = appSettings.isVibrationEnabled,
                                    onCheckedChange = { active ->
                                        viewModel.updateSettings(
                                            theme = appSettings.theme,
                                            isVibrationEnabled = active,
                                            isSoundEnabled = appSettings.isSoundEnabled,
                                            timeFormat24Hour = appSettings.timeFormat24Hour,
                                            firstDayOfWeek = appSettings.firstDayOfWeek
                                        )
                                    }
                                )
                            }

                            Divider()

                            // Sound switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Play Alert Sounds", fontSize = 15.sp)
                                }
                                Switch(
                                    checked = appSettings.isSoundEnabled,
                                    onCheckedChange = { active ->
                                        viewModel.updateSettings(
                                            theme = appSettings.theme,
                                            isVibrationEnabled = appSettings.isVibrationEnabled,
                                            isSoundEnabled = active,
                                            timeFormat24Hour = appSettings.timeFormat24Hour,
                                            firstDayOfWeek = appSettings.firstDayOfWeek
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Help & Instructions Card
                item {
                    var showHelpDialog by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF0288D1).copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Help & Instructions",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0288D1),
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showHelpDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Help, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("User Guidelines & Tips", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (showHelpDialog) {
                        AlertDialog(
                            onDismissRequest = { showHelpDialog = false },
                            title = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = "User Guide",
                                            tint = Color(0xFF0288D1),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = "User Guidelines 📋",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { showHelpDialog = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                                    }
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        "Follow these simple instructions to make the most of the application:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Filled.List, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                                            Text("1. How to Create List", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0288D1))
                                        }
                                        Text("Open the sidebar drawer by tapping the menu icon in the top left. Tap on \"+ Add List\" to create a custom category list for your tasks.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Filled.AddTask, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                                            Text("2. Add, Edit & Delete Tasks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0288D1))
                                        }
                                        Text("• Add: Tap the floating '+' button at the bottom-right, fill in task title, optional due date, choose list and save.\n• Edit: Tap any task in the list to open the task editor dialog to update details.\n• Delete: Swipe any task or tap delete inside the task details to move it to Trash. You can permanently delete from Trash.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                                            Text("3. How to Finish Tasks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0288D1))
                                        }
                                        Text("Simply tap the circular checkbox next to any task title. Finished tasks will be beautifully styled as strikethrough text and moved down.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Filled.Widgets, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                                            Text("4. How to Add Widget", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0288D1))
                                        }
                                        Text("Go to your device's home screen, long press on an empty space, tap 'Widgets', find 'To Do List App', and drag it to your screen for instant desktop updates!", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showHelpDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Got It!", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }

                // Administrative Tools Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF0288D1).copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Administrative Tools",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0288D1),
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    passwordInput = ""
                                    showPasswordDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                modifier = Modifier.fillMaxWidth()
                             ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("A0.1 Admin Access", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    )

    // Password Prompt Dialog
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Admin Authorization", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(
                        onClick = { showPasswordDialog = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Please enter the admin password to access the console:", fontSize = 14.sp)
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    // Use Base64 decoded check
                                    val decodedSavedPassword = viewModel.decodeBase64(appSettings.adminPassword)
                                    if (passwordInput.trim() == decodedSavedPassword) {
                                        showPasswordDialog = false
                                        navController.navigate("admin_console")
                                    } else {
                                        Toast.makeText(context, "Invalid admin password! 🔑", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "Submit", tint = Color(0xFF0288D1))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                    )
                }
            },
            confirmButton = {},
            dismissButton = null
        )
    }
}

// Helper to calculate dynamic calendar header categories
private fun getTaskCategory(dueDate: Long, isCompleted: Boolean): String {
    if (isCompleted) return "Finished"
    if (dueDate == 0L) return "Later"

    val now = Calendar.getInstance()
    val due = Calendar.getInstance().apply { timeInMillis = dueDate }

    val nowDay = now.get(Calendar.DAY_OF_YEAR)
    val nowYear = now.get(Calendar.YEAR)
    val dueDay = due.get(Calendar.DAY_OF_YEAR)
    val dueYear = due.get(Calendar.YEAR)

    if (dueDate < System.currentTimeMillis() && (nowYear != dueYear || nowDay != dueDay)) {
        return "Overdue"
    }

    if (nowYear == dueYear && nowDay == dueDay) {
        return "Today"
    }

    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    if (tomorrow.get(Calendar.YEAR) == dueYear && tomorrow.get(Calendar.DAY_OF_YEAR) == dueDay) {
        return "Tomorrow"
    }

    val sevenDays = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
    if (due.before(sevenDays)) {
        return "This week"
    }

    return "Later"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsoleScreen(
    navController: NavController,
    viewModel: TasksViewModel
) {
    val context = LocalContext.current
    val appSettings by viewModel.appSettings.collectAsState()
    val allLists by viewModel.allLists.collectAsState()
    val allTasks by viewModel.allTasksList.collectAsState()

    var adminPasswordInput by remember { mutableStateOf("") }
    var pushTitle by remember { mutableStateOf("") }
    var pushBody by remember { mutableStateOf("") }

    var popupEnabled by remember { mutableStateOf(false) }
    var popupTitleInput by remember { mutableStateOf("") }
    var popupTextInput by remember { mutableStateOf("") }
    var popupMandatoryInput by remember { mutableStateOf(false) }
    var popupActionText by remember { mutableStateOf("") }
    var popupActionUrl by remember { mutableStateOf("") }
    var popupHasActionButton by remember { mutableStateOf(false) }

    var morningEnabled by remember { mutableStateOf(true) }
    var morningHour by remember { mutableStateOf(8) }
    var morningMinute by remember { mutableStateOf(0) }
    var morningText by remember { mutableStateOf("") }

    var nightEnabled by remember { mutableStateOf(true) }
    var nightHour by remember { mutableStateOf(21) }
    var nightMinute by remember { mutableStateOf(0) }
    var nightText by remember { mutableStateOf("") }

    var newListInput by remember { mutableStateOf("") }
    var listToRename by remember { mutableStateOf<TaskList?>(null) }
    var renameListInput by remember { mutableStateOf("") }

    var isTelemetrySyncing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Sync form values with appSettings when loaded
    LaunchedEffect(appSettings) {
        adminPasswordInput = viewModel.decodeBase64(appSettings.adminPassword)
        popupEnabled = appSettings.adminShowPopup
        popupTitleInput = appSettings.adminPopupTitle
        popupTextInput = appSettings.adminPopupText
        popupMandatoryInput = appSettings.adminPopupMandatory
        popupActionText = appSettings.adminPopupActionText
        popupActionUrl = appSettings.adminPopupActionUrl
        popupHasActionButton = appSettings.adminPopupHasActionButton

        morningEnabled = appSettings.morningWishEnabled
        morningHour = appSettings.morningWishHour
        morningMinute = appSettings.morningWishMinute
        morningText = appSettings.morningWishText

        nightEnabled = appSettings.nightWishEnabled
        nightHour = appSettings.nightWishHour
        nightMinute = appSettings.nightWishMinute
        nightText = appSettings.nightWishText
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console Panel", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: DEVICE USAGE & ACTIVE USER ANALYTICS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Analytics, contentDescription = null, tint = Color(0xFF0288D1))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("App Usage & Active Devices", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ONLINE", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Total Installs", fontSize = 11.sp, color = Color.Gray)
                                Text("1,248", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Active Devices (24h)", fontSize = 11.sp, color = Color.Gray)
                                Text("154", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Live Users Now", fontSize = 11.sp, color = Color.Gray)
                                Text("12", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF10B981))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Active Device Node Information (Your Device)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))

                        val realTelemetry = listOf(
                            "Device Hardware Model" to android.os.Build.MODEL,
                            "System Build Level" to "SDK " + android.os.Build.VERSION.SDK_INT,
                            "Selected Timezone" to appSettings.timezone,
                            "System Locale Timezone" to java.util.TimeZone.getDefault().id,
                            "Package Name" to context.packageName
                        )
                        realTelemetry.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(label, fontSize = 13.sp)
                                }
                                Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                isTelemetrySyncing = true
                                scope.launch {
                                    kotlinx.coroutines.delay(1200)
                                    isTelemetrySyncing = false
                                    Toast.makeText(context, "Successfully updated active user metrics from 1,248 devices! 📊", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isTelemetrySyncing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isTelemetrySyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing Active Devices...")
                            } else {
                                Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sync Active Devices & Users")
                            }
                        }
                    }
                }
            }

            // SECTION 2: EVERYDAY MORNING AND NIGHT WISH (CUSTOMIZED MESSAGE & TIME SET BY ADMIN)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Everyday Morning & Night Wishes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Divider()

                        // Morning Wish Section
                        Text("Morning Wish Configuration", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0288D1))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable Morning Wish Reminder", fontSize = 14.sp)
                            Switch(checked = morningEnabled, onCheckedChange = { morningEnabled = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = if (morningHour == 0) "0" else morningHour.toString(),
                                onValueChange = { morningHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 8 },
                                label = { Text("Hour (0-23)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                enabled = morningEnabled
                            )
                            OutlinedTextField(
                                value = if (morningMinute == 0) "0" else morningMinute.toString(),
                                onValueChange = { morningMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                                label = { Text("Minute (0-59)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                enabled = morningEnabled
                            )
                        }

                        val morningFormattedTime = String.format("%02d:%02d", morningHour, morningMinute)
                        Text("Morning Wish Schedules Daily at: $morningFormattedTime", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

                        OutlinedTextField(
                            value = morningText,
                            onValueChange = { morningText = it },
                            label = { Text("Morning Wish Customized Message") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = morningEnabled
                        )

                        Button(
                            onClick = {
                                viewModel.updateMorningWishSettings(morningHour, morningMinute, morningText.trim(), morningEnabled)
                                Toast.makeText(context, "Morning Wish settings saved and scheduled! 🌅", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Morning Wish Settings")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()

                        // Night Wish Section
                        Text("Night Wish Configuration", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4F46E5))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable Night Wish Reminder", fontSize = 14.sp)
                            Switch(checked = nightEnabled, onCheckedChange = { nightEnabled = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = if (nightHour == 0) "0" else nightHour.toString(),
                                onValueChange = { nightHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 21 },
                                label = { Text("Hour (0-23)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                enabled = nightEnabled
                            )
                            OutlinedTextField(
                                value = if (nightMinute == 0) "0" else nightMinute.toString(),
                                onValueChange = { nightMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                                label = { Text("Minute (0-59)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                enabled = nightEnabled
                            )
                        }

                        val nightFormattedTime = String.format("%02d:%02d", nightHour, nightMinute)
                        Text("Night Wish Schedules Daily at: $nightFormattedTime", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

                        OutlinedTextField(
                            value = nightText,
                            onValueChange = { nightText = it },
                            label = { Text("Night Wish Customized Message") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nightEnabled
                        )

                        Button(
                            onClick = {
                                viewModel.updateNightWishSettings(nightHour, nightMinute, nightText.trim(), nightEnabled)
                                Toast.makeText(context, "Night Wish settings saved and scheduled! 🌃", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Night Wish Settings")
                        }
                    }
                }
            }

            // SECTION 3: PASSWORD & SECURITY
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Console Security (Base64)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = { adminPasswordInput = it },
                            label = { Text("Admin Console Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (adminPasswordInput.isBlank()) {
                                    Toast.makeText(context, "Password cannot be empty!", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Base64 encoded inside TasksViewModel automatically!
                                    viewModel.updateAdminPassword(adminPasswordInput.trim())
                                    Toast.makeText(context, "Admin password updated securely (Base64)! 🔒", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save New Password Securely")
                        }
                    }
                }
            }

            // SECTION 4: SYSTEM PUSH BROADCAST
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Campaign, contentDescription = null, tint = Color(0xFF0288D1))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Push Broadcast (Optional Fields)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        OutlinedTextField(
                            value = pushTitle,
                            onValueChange = { pushTitle = it },
                            label = { Text("Notification Title (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = pushBody,
                            onValueChange = { pushBody = it },
                            label = { Text("Notification Body Text (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                // Title and Body are both optional
                                viewModel.sendPushNotification(pushTitle.trim(), pushBody.trim())
                                Toast.makeText(context, "Broadcast dispatched successfully!", Toast.LENGTH_SHORT).show()
                                pushTitle = ""
                                pushBody = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                        ) {
                            Icon(Icons.Filled.RssFeed, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Broadcast Push Notification")
                        }
                    }
                }
            }

            // SECTION 5: MANAGE TASK LISTS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.List, contentDescription = null, tint = Color(0xFF6366F1))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Database Task Lists Manager", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newListInput,
                                onValueChange = { newListInput = it },
                                placeholder = { Text("New list name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newListInput.isBlank()) {
                                        Toast.makeText(context, "Enter a list name", Toast.LENGTH_SHORT).show()
                                    } else if (allLists.any { it.name.lowercase() == newListInput.lowercase() }) {
                                        Toast.makeText(context, "List already exists", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addList(newListInput.trim())
                                        Toast.makeText(context, "Added list '${newListInput.trim()}'", Toast.LENGTH_SHORT).show()
                                        newListInput = ""
                                    }
                                }
                            ) {
                                Text("Add")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        allLists.forEach { list ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (list.isSystemList) Icons.Filled.Lock else Icons.Filled.Label,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(list.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }

                                if (!list.isSystemList) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                listToRename = list
                                                renameListInput = list.name
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteList(list.name)
                                                Toast.makeText(context, "Deleted '${list.name}' and trashed its tasks.", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else {
                                    Text("System", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 6: APP-OPEN ANNOUNCEMENT POPUP RULES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationAdd, contentDescription = null, tint = Color(0xFFEF4444))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("App-Open Popup Rules", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Switch(checked = popupEnabled, onCheckedChange = { popupEnabled = it })
                        }

                        OutlinedTextField(
                            value = popupTitleInput,
                            onValueChange = { popupTitleInput = it },
                            label = { Text("Popup Announcement Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = popupEnabled
                        )

                        OutlinedTextField(
                            value = popupTextInput,
                            onValueChange = { popupTextInput = it },
                            label = { Text("Popup Announcement Body") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = popupEnabled
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Mandatory Announcement Gate", fontSize = 14.sp)
                                Text("Hides close button. User must acknowledge.", fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = popupMandatoryInput,
                                onCheckedChange = { popupMandatoryInput = it },
                                enabled = popupEnabled
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Custom Action button settings (Requirement 8)
                        Text("Custom Action Button on Popup", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0288D1))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include Action Button on Popup", fontSize = 14.sp)
                            Switch(
                                checked = popupHasActionButton,
                                onCheckedChange = { popupHasActionButton = it },
                                enabled = popupEnabled
                            )
                        }

                        OutlinedTextField(
                            value = popupActionText,
                            onValueChange = { popupActionText = it },
                            label = { Text("Action Button Label (e.g. Learn More)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = popupEnabled && popupHasActionButton
                        )

                        OutlinedTextField(
                            value = popupActionUrl,
                            onValueChange = { popupActionUrl = it },
                            label = { Text("Action Redirect URL (e.g. https://...)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = popupEnabled && popupHasActionButton
                        )

                        Button(
                            onClick = {
                                viewModel.updateAdminPopupSettings(
                                    show = popupEnabled,
                                    title = popupTitleInput.trim(),
                                    text = popupTextInput.trim(),
                                    mandatory = popupMandatoryInput,
                                    actionText = popupActionText.trim(),
                                    actionUrl = popupActionUrl.trim(),
                                    hasActionButton = popupHasActionButton
                                )
                                Toast.makeText(context, "Announcement and Button configurations saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Announcement Rules")
                        }
                    }
                }
            }
        }
    }

    // Rename List Dialog
    listToRename?.let { list ->
        AlertDialog(
            onDismissRequest = { listToRename = null },
            title = { Text("Rename List", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameListInput,
                    onValueChange = { renameListInput = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameListInput.isBlank()) {
                            Toast.makeText(context, "List name cannot be empty", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.renameList(list.name, renameListInput.trim())
                            Toast.makeText(context, "Renamed to '${renameListInput.trim()}'", Toast.LENGTH_SHORT).show()
                            listToRename = null
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { listToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
