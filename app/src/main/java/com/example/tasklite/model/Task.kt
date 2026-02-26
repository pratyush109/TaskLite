package com.example.tasklite.model

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String? = null
)