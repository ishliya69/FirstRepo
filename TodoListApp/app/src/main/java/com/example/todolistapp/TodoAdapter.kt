package com.example.todolistapp

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.databinding.ItemTodoBinding

interface OnItemClickListener {
    fun onItemClick(todoItem: TodoItem, position: Int)
}

class TodoAdapter(
    private val todoItems: MutableList<TodoItem>
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null
    var onItemCheckedChanged: ((TodoItem) -> Unit)? = null // New lambda property

    inner class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(todoItems[adapterPosition], adapterPosition)
                }
            }

            binding.checkBoxTodoCompleted.setOnCheckedChangeListener { _, isChecked ->
                // Ensure adapterPosition is valid before accessing todoItems
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val item = todoItems[adapterPosition]
                    item.isCompleted = isChecked // Update the underlying data
                    onItemCheckedChanged?.invoke(item) // Invoke the new lambda

                    // Apply/remove strikethrough directly based on the new state
                    updateTitleStrikeThrough(isChecked)
                }
            }
        }

        fun bind(todoItem: TodoItem) {
            binding.textViewTodoTitle.text = todoItem.title
            updateTitleStrikeThrough(todoItem.isCompleted)

            if (todoItem.description.isNullOrEmpty()) {
                binding.textViewTodoDescription.visibility = View.GONE
            } else {
                binding.textViewTodoDescription.visibility = View.VISIBLE
                binding.textViewTodoDescription.text = todoItem.description
            }
            // Set checked state without triggering the listener to avoid loops if listener also calls bind
            binding.checkBoxTodoCompleted.setOnCheckedChangeListener(null)
            binding.checkBoxTodoCompleted.isChecked = todoItem.isCompleted
            // Re-attach the listener
            binding.checkBoxTodoCompleted.setOnCheckedChangeListener { _, isChecked ->
                 if (adapterPosition != RecyclerView.NO_POSITION) {
                    val item = todoItems[adapterPosition]
                    item.isCompleted = isChecked
                    onItemCheckedChanged?.invoke(item) // Also invoke here for consistency
                    updateTitleStrikeThrough(isChecked)
                }
            }

            // Handle Due Date display
            if (todoItem.dueDate != null) {
                val sdf = java.text.SimpleDateFormat("EEE, MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                binding.textViewItemDueDate.text = "Due: ${sdf.format(todoItem.dueDate)}"
                binding.textViewItemDueDate.visibility = View.VISIBLE
            } else {
                binding.textViewItemDueDate.visibility = View.GONE
            }
        }

        private fun updateTitleStrikeThrough(isCompleted: Boolean) {
            if (isCompleted) {
                binding.textViewTodoTitle.paintFlags = binding.textViewTodoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.textViewTodoTitle.paintFlags = binding.textViewTodoTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentItem = todoItems[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return todoItems.size
    }

    // Helper functions
    fun addTodo(todoItem: TodoItem) {
        todoItems.add(todoItem)
        notifyItemInserted(todoItems.size - 1)
    }

    fun removeTodoAt(position: Int) {
        if (position >= 0 && position < todoItems.size) {
            todoItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateTodoAt(position: Int, todoItem: TodoItem) {
        if (position >= 0 && position < todoItems.size) {
            todoItems[position] = todoItem
            notifyItemChanged(position)
        }
    }

    fun getItems(): List<TodoItem> {
        return todoItems
    }

    fun getItemAt(position: Int): TodoItem? {
        return todoItems.getOrNull(position)
    }

    fun updateItems(newItems: List<TodoItem>) {
        todoItems.clear()
        todoItems.addAll(newItems)
        notifyDataSetChanged() // Consider DiffUtil for better performance later
    }
}
