@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasklite.model.Task
import com.example.tasklite.ui.theme.TaskLiteTheme
import com.example.tasklite.ui.theme.White
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.example.tasklite.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// --- DASHBOARD ACTIVITY ---
class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskLiteTheme {
                val taskViewModel: TaskViewModel = viewModel()
                DashboardBody(taskViewModel)
            }
        }
    }
}

// --- DASHBOARD BODY ---
@Composable
fun DashboardBody(taskViewModel: TaskViewModel) {
    val navItems = listOf("Home", "Notification", "Profile", "Calendar")
    var selectedIndex by remember { mutableStateOf(0) }
    val tasks by taskViewModel.tasks.collectAsState()
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            if (selectedIndex == 0) {
                FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, contentDescription = "Add Task") }
            }
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = {
                            if (label == "Notification" && tasks.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text(tasks.size.toString()) } }) {
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedIndex) {
                0 -> HomeScreen(taskViewModel)
                1 -> NotificationScreen(taskViewModel)
                2 -> ProfileScreen(email = FirebaseAuth.getInstance().currentUser?.email) {
                    FirebaseAuth.getInstance().signOut()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                }
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
    var status by remember { mutableStateOf("Pending") }
    var category by remember { mutableStateOf("Personal") }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val statusOptions = listOf("Pending", "In Progress", "Completed")
    val categoryOptions = listOf("Work", "Personal", "Shopping", "Other")
    val tasks by viewModel.tasks.collectAsState()


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(title, { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(description, { description = it }, label = { Text("Task Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ISO_DATE),
                onValueChange = {},
                label = { Text("Due Date") },
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Pick Date") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.matchParentSize().clickable {
                DatePickerDialog(
                    context,
                    { _, y, m, d -> selectedDate = LocalDate.of(y, m + 1, d) },
                    selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
                ).show()
            })
        }
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }) {
            TextField(value = status, onValueChange = {}, readOnly = true, label = { Text("Status") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedStatus) }, modifier = Modifier.fillMaxWidth().menuAnchor())
            ExposedDropdownMenu(expandedStatus, onDismissRequest = { expandedStatus = false }) {
                statusOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { status = option; expandedStatus = false }) }
            }
        }
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
            TextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory) }, modifier = Modifier.fillMaxWidth().menuAnchor())
            ExposedDropdownMenu(expandedCategory, onDismissRequest = { expandedCategory = false }) {
                categoryOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { category = option; expandedCategory = false }) }
            }
        }
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && description.isNotBlank()) {
                    viewModel.addTask(title, description, selectedDate.toString(), status, category)
                    title = ""
                    description = ""
                    selectedDate = LocalDate.now()
                    status = "Pending"
                    category = "Personal"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add Task") }

        Spacer(Modifier.height(16.dp))
        LazyColumn { items(tasks) { task -> TaskItem(task, viewModel) } }
    }
}

// --- TASK ITEM ---
@Composable
fun TaskItem(task: Task, viewModel: TaskViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(task.title) }
    var editDesc by remember { mutableStateOf(task.description) }
    var editDate by remember { mutableStateOf(task.dueDate ?: "") }
    var editStatus by remember { mutableStateOf(task.status ?: "Pending") }
    var editCategory by remember { mutableStateOf(task.category ?: "Personal") }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val statusOptions = listOf("Pending", "In Progress", "Completed")
    val categoryOptions = listOf("Work", "Personal", "Shopping", "Other")

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isEditing) {
                OutlinedTextField(editTitle, { editTitle = it }, label = { Text("Edit Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(editDesc, { editDesc = it }, label = { Text("Edit Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(editDate, {}, label = { Text("Edit Due Date") }, modifier = Modifier.fillMaxWidth().clickable {
                    val current = if (editDate.isNotEmpty()) LocalDate.parse(editDate) else LocalDate.now()
                    DatePickerDialog(context, { _, y, m, d -> editDate = LocalDate.of(y, m + 1, d).toString() }, current.year, current.monthValue - 1, current.dayOfMonth).show()
                }, readOnly = true)

                ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }) {
                    TextField(editStatus, {}, label = { Text("Status") }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedStatus) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expandedStatus, onDismissRequest = { expandedStatus = false }) {
                        statusOptions.forEach { option -> DropdownMenuItem({ Text(option) }, onClick = { editStatus = option; expandedStatus = false }) }
                    }
                }

                ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
                    TextField(editCategory, {}, label = { Text("Category") }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expandedCategory, onDismissRequest = { expandedCategory = false }) {
                        categoryOptions.forEach { option -> DropdownMenuItem({ Text(option) }, onClick = { editCategory = option; expandedCategory = false }) }
                    }
                }

                Row {
                    Button({ viewModel.updateTask(task.id, editTitle, editDesc, editDate, editStatus, editCategory); isEditing = false }) { Text("Save") }
                    Spacer(Modifier.width(8.dp))
                    Button({ isEditing = false }) { Text("Cancel") }
                }
            } else {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text(task.description, style = MaterialTheme.typography.bodyMedium)
                if (task.dueDate != null) Text("Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall)
                if (task.status != null) Text("Status: ${task.status}", style = MaterialTheme.typography.bodySmall)
                if (task.category != null) Text("Category: ${task.category}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Row {
                    Button({ isEditing = true }) { Text("Edit") }
                    Spacer(Modifier.width(8.dp))
                    Button({ viewModel.deleteTask(task.id) }) { Text("Delete") }
                }
            }
        }
    }
}

// --- NOTIFICATIONS ---
@Composable
fun NotificationScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notifications", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        val recentTasks = tasks.takeLast(5)
        if (recentTasks.isEmpty()) Text("No new notifications.")
        else LazyColumn { items(recentTasks) { task -> Text("- ${task.title} [${task.status}]") } }
    }
}

// --- PROFILE ---
@Composable
fun ProfileScreen(email: String?, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text("Logged in as:", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(email ?: "No user", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
    }
}

// --- CALENDAR ---
@Composable
fun CalendarScreen(viewModel: TaskViewModel) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val tasks by viewModel.tasks.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")), style = MaterialTheme.typography.headlineMedium)
            Row {
                IconButton({ currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.Default.ArrowBack, "") }
                IconButton({ currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.Default.ArrowForward, "") }
            }
        }
        Spacer(Modifier.height(16.dp))
        val firstDay = currentMonth.atDay(1)
        val lastDay = currentMonth.atEndOfMonth()
        val days = (1..lastDay.dayOfMonth).map { firstDay.withDayOfMonth(it) }
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(days) { day ->
                val dayTasks = tasks.filter { it.dueDate != null && LocalDate.parse(it.dueDate) == day }
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .background(if (dayTasks.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                        .clickable { },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(day.dayOfMonth.toString())
                    dayTasks.forEach { t -> Text("${t.title} [${t.status}]", style = MaterialTheme.typography.bodySmall, maxLines = 1) }
                }
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    TaskLiteTheme {
        val fakeViewModel = object {
            val tasks = mutableStateListOf(
                Task(id = "1", title = "Buy groceries", description = "Milk, bread, eggs", dueDate = "2024-02-26", status = "Pending", category = "Shopping"),
                Task(id = "2", title = "Walk the dog", description = "30 mins in park", dueDate = "2024-02-26", status = "Completed", category = "Personal")
            )
            fun addTask(task: Task) { tasks.add(task) }
            fun updateTask(id: String, title: String, description: String) {
                val index = tasks.indexOfFirst { it.id == id }
                if (index >= 0) tasks[index] = tasks[index].copy(title = title, description = description)
            }
            fun deleteTask(id: String) { tasks.removeAll { it.id == id } }
        }
        DashboardBody(fakeViewModel as TaskViewModel) // Just for preview typecasting
    }
}