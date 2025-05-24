package com.example.todolistapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todoItem: TodoItem): Long // Changed return type

    @Update
    suspend fun update(todoItem: TodoItem)

    @Delete
    suspend fun delete(todoItem: TodoItem)

    // --- Methods for different sort orders ---

    // Sort by Title
    @Query("SELECT * FROM todo_items WHERE (:isCompletedFilter IS NULL OR isCompleted = :isCompletedFilter) ORDER BY title ASC")
    fun getAllTodoItemsByTitleAsc(isCompletedFilter: Boolean?): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE (:isCompletedFilter IS NULL OR isCompleted = :isCompletedFilter) ORDER BY title DESC")
    fun getAllTodoItemsByTitleDesc(isCompletedFilter: Boolean?): Flow<List<TodoItem>>

    // Sort by Creation Date
    @Query("SELECT * FROM todo_items WHERE (:isCompletedFilter IS NULL OR isCompleted = :isCompletedFilter) ORDER BY createdAt ASC")
    fun getAllTodoItemsByDateAsc(isCompletedFilter: Boolean?): Flow<List<TodoItem>>

    @Query("SELECT * FROM todo_items WHERE (:isCompletedFilter IS NULL OR isCompleted = :isCompletedFilter) ORDER BY createdAt DESC")
    fun getAllTodoItemsByDateDesc(isCompletedFilter: Boolean?): Flow<List<TodoItem>> // Original default

    // Sort by Completion Status (with secondary sort by date)
    // isCompleted ASC: false (0) before true (1) -> Pending items first
    @Query("SELECT * FROM todo_items WHERE (:isCompletedFilter IS NULL OR isCompleted = :isCompletedFilter) ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTodoItemsByCompletionStatusAsc(isCompletedFilter: Boolean?): Flow<List<TodoItem>>

    // isCompleted DESC: true (1) before false (0) -> Completed items first
    @Query("SELECT * FROM todo_items WHERE (:isCompletedFilter IS NULL OR isCompleted = :isCompletedFilter) ORDER BY isCompleted DESC, createdAt DESC")
    fun getAllTodoItemsByCompletionStatusDesc(isCompletedFilter: Boolean?): Flow<List<TodoItem>>

    // --- End of sort order methods ---

    @Query("SELECT * FROM todo_items WHERE id = :id")
    suspend fun getTodoItemById(id: Long): TodoItem?
}
