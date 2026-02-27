package com.example.tasklite.model

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String? = null,
    val status: String? = "Pending",
    val category: String? = "Personal",
    val createdAt: Long = System.currentTimeMillis() // add this
)