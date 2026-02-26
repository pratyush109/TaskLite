package com.example.tasklite.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.tasklite.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class TaskViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("tasks")
    var tasks = mutableStateListOf<Task>()
        private set

    init {
        auth.currentUser?.let { user ->
            database.child(user.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tasks.clear()
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        task?.let { tasks.add(it) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun addTask(title: String, description: String, dueDate: String?) {
        val task = Task(UUID.randomUUID().toString(), title, description, dueDate)
        auth.currentUser?.let { user ->
            database.child(user.uid).child(task.id).setValue(task)
        }
    }

    fun updateTask(taskId: String, newTitle: String, newDescription: String, newDueDate: String?) {
        val task = Task(taskId, newTitle, newDescription, newDueDate)
        auth.currentUser?.let { user ->
            database.child(user.uid).child(taskId).setValue(task)
        }
    }

    fun deleteTask(taskId: String) {
        auth.currentUser?.let { user ->
            database.child(user.uid).child(taskId).removeValue()
        }
    }
}