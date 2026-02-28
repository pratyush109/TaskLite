package com.example.tasklite.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tasklite.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TaskViewModel(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        // Listen to Firestore changes
        db.collection("tasks").addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                _tasks.value = snapshot.documents.map { doc ->
                    Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        dueDate = doc.getString("dueDate"),
                        status = doc.getString("status"),
                        category = doc.getString("category")
                    )
                }
            }
        }
    }

    fun addTask(title: String, description: String, dueDate: String, status: String, category: String) {
        val newTask = hashMapOf(
            "title" to title,
            "description" to description,
            "dueDate" to dueDate,
            "status" to status,
            "category" to category
        )
        db.collection("tasks").add(newTask)
    }

    fun updateTask(id: String, title: String, description: String, dueDate: String, status: String, category: String) {
        val updatedTask = hashMapOf(
            "title" to title,
            "description" to description,
            "dueDate" to dueDate,
            "status" to status,
            "category" to category
        )
        db.collection("tasks").document(id).set(updatedTask)
    }

    fun deleteTask(id: String) {
        db.collection("tasks").document(id).delete()
    }
}