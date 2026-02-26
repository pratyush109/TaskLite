package com.example.tasklite.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.tasklite.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class TaskViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    open val tasks: StateFlow<List<Task>> = _tasks

    init {
        uid?.let { loadTasks(it) }
    }

    private fun loadTasks(userId: String) {
        db.collection("users").document(userId).collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _tasks.value = list
            }
    }

    open fun addTask(title: String, description: String, dueDate: String, status: String, category: String) {
        val userId = uid ?: return
        val newTask = Task("", title, description, dueDate, status, category)
        db.collection("users").document(userId).collection("tasks")
            .add(newTask)
    }

    open fun updateTask(id: String, title: String, description: String, dueDate: String, status: String, category: String) {
        val userId = uid ?: return
        db.collection("users").document(userId).collection("tasks").document(id)
            .set(Task(id, title, description, dueDate, status, category))
    }

    open fun deleteTask(id: String) {
        val userId = uid ?: return
        db.collection("users").document(userId).collection("tasks").document(id)
            .delete()
    }
}