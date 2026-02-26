package com.example.tasklite.repository

import com.example.tasklite.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TaskRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private fun userTaskRef(): DatabaseReference {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
        return database.child("tasks").child(uid)
    }

    fun addTask(task: Task) {
        val key = userTaskRef().push().key ?: return
        userTaskRef().child(key).setValue(task.copy(id = key))
    }

    fun updateTask(task: Task) {
        userTaskRef().child(task.id).setValue(task)
    }

    fun deleteTask(taskId: String) {
        userTaskRef().child(taskId).removeValue()
    }

    fun observeTasks(onChange: (List<Task>) -> Unit) {
        userTaskRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Task>()

                for (child in snapshot.children) {
                    val task = child.getValue(Task::class.java)
                    task?.let { list.add(it) }
                }

                onChange(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}