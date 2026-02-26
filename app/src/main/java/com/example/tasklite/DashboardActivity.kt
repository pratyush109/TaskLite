package com.example.tasklite

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasklite.model.Task
import com.example.tasklite.ui.theme.TaskLiteTheme
import com.example.tasklite.ui.theme.White
import com.example.tasklite.viewmodel.AuthViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskLiteTheme {
                val taskViewModel: TaskViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()
                DashboardBody(taskViewModel, authViewModel)
            }
        }
    }
}

// Updated ViewModel
class TaskViewModel : ViewModel() {
    var tasks = mutableStateListOf<Task>()
        private set

    fun addTask(title: String, description: String, dueDate: String?) {
        tasks.add(Task(UUID.randomUUID().toString(), title, description, dueDate))
    }

    fun deleteTask(taskId: String) {
        tasks.removeAll { it.id == taskId }
    }

    fun updateTask(taskId: String, newTitle: String, newDescription: String, newDueDate: String?) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(title = newTitle, description = newDescription, dueDate = newDueDate)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody(taskViewModel: TaskViewModel, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val navItems = listOf("Home", "Notification", "Profile", "Calendar")
    var selectedIndex by remember { mutableStateOf(0) }

    val tasks by remember { derivedStateOf { taskViewModel.tasks } }

    Scaffold(
        floatingActionButton = {
            if (selectedIndex == 0) {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = {
                            if (label == "Notification" && tasks.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text(tasks.size.toString())
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = label)
                                }
                            } else {
                                Icon(
                                    imageVector = when (label) {
                                        "Home" -> Icons.Default.Home
                                        "Notification" -> Icons.Default.Notifications
                                        "Profile" -> Icons.Default.Person
                                        "Calendar" -> Icons.Default.DateRange
                                        else -> Icons.Default.Add
                                    },
                                    contentDescription = label
                                )
                            }
                        },
                        label = { Text(label) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedIndex) {
                0 -> HomeScreen(viewModel = taskViewModel)
                1 -> NotificationScreen(viewModel = taskViewModel)
                2 -> ProfileScreen(
                    email = authViewModel.getCurrentUserEmail(),
                    onLogout = {
                        authViewModel.logout()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    }
                )
                3 -> CalendarScreen(taskViewModel)
            }
        }
    }
}

// --- HOME SCREEN ---
@Composable
fun HomeScreen(viewModel: TaskViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Task Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = selectedDate.format(DateTimeFormatter.ISO_DATE),
            onValueChange = {},
            label = { Text("Due Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val picker = DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            selectedDate = LocalDate.of(year, month + 1, day)
                        },
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth
                    )
                    picker.show()
                },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && description.isNotBlank()) {
                    viewModel.addTask(title, description, selectedDate.format(DateTimeFormatter.ISO_DATE))
                    title = ""
                    description = ""
                    selectedDate = LocalDate.now()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(viewModel.tasks) { task ->
                TaskItem(task = task, viewModel = viewModel)
            }
        }
    }
}

// --- TASK ITEM ---
@Composable
fun TaskItem(task: Task, viewModel: TaskViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(task.title) }
    var editDesc by remember { mutableStateOf(task.description) }
    var editDate by remember { mutableStateOf(task.dueDate ?: "") }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isEditing) {
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = { Text("Edit Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editDesc,
                    onValueChange = { editDesc = it },
                    label = { Text("Edit Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editDate,
                    onValueChange = {},
                    label = { Text("Edit Due Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val current = if (editDate.isNotEmpty()) LocalDate.parse(editDate) else LocalDate.now()
                            val picker = DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    editDate = LocalDate.of(year, month + 1, day).format(DateTimeFormatter.ISO_DATE)
                                },
                                current.year,
                                current.monthValue - 1,
                                current.dayOfMonth
                            )
                            picker.show()
                        },
                    readOnly = true
                )
                Row {
                    Button(onClick = {
                        viewModel.updateTask(task.id, editTitle, editDesc, editDate)
                        isEditing = false
                    }) { Text("Save") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { isEditing = false }) { Text("Cancel") }
                }
            } else {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text(task.description, style = MaterialTheme.typography.bodyMedium)
                if (task.dueDate != null) Text("Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Button(onClick = { isEditing = true }) { Text("Edit") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.deleteTask(task.id) }) { Text("Delete") }
                }
            }
        }
    }
}

// --- NOTIFICATIONS ---
@Composable
fun NotificationScreen(viewModel: TaskViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Notifications", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        val tasks = viewModel.tasks.takeLast(5)
        if (tasks.isEmpty()) Text("No new notifications.")
        else LazyColumn { items(tasks) { task -> Text("- ${task.title}: ${task.description}") } }
    }
}

// --- PROFILE ---
@Composable
fun ProfileScreen(email: String?, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Logged in as:", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(email ?: "No user", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
    }
}

// --- CALENDAR ---
@Composable
fun CalendarScreen(viewModel: TaskViewModel) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val tasks = viewModel.tasks

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.headlineMedium)
            Row {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.Default.ArrowBack, "") }
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.Default.ArrowForward, "") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val firstDay = currentMonth.atDay(1)
        val lastDay = currentMonth.atEndOfMonth()
        val days = (1..lastDay.dayOfMonth).map { firstDay.withDayOfMonth(it) }

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(days) { day ->
                val dayTasks = tasks.filter { task ->
                    task.dueDate != null && LocalDate.parse(task.dueDate) == day
                }
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .background(if (dayTasks.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                        .clickable { },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(day.dayOfMonth.toString())
                    dayTasks.forEach { task -> Text(task.title, style = MaterialTheme.typography.bodySmall, maxLines = 1) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    TaskLiteTheme {
        val taskViewModel: TaskViewModel = viewModel()
        val authViewModel: AuthViewModel = viewModel()
        DashboardBody(taskViewModel, authViewModel)
    }
}