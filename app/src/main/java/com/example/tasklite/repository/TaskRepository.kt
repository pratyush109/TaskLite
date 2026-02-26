package com.example.tasklite.repository

import com.example.tasklite.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TaskRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private fun getUserTaskRef(): DatabaseReference {
        val uid = auth.currentUser?.uid ?: return database
        return database.child("tasks").child(uid)
    }

    fun addTask(task: Task) {
        val key = getUserTaskRef().push().key ?: return
        getUserTaskRef().child(key).setValue(task.copy(id = key))
    }

    fun updateTask(task: Task) {
        getUserTaskRef().child(task.id).setValue(task)
    }

    fun deleteTask(taskId: String) {
        getUserTaskRef().child(taskId).removeValue()
    }

    fun observeTasks(listener: (List<Task>) -> Unit) {
        getUserTaskRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Task>()
                for (child in snapshot.children) {
                    val task = child.getValue(Task::class.java)
                    task?.let { list.add(it) }
                }
                listener(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}