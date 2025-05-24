package com.example.todolistapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, // Default to 0L for Room to auto-generate IDs for new items
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null // New property for due date
)
