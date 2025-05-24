package com.example.todolistapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.todolistapp.databinding.ActivityAddEditTodoBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTodoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "com.example.todolistapp.EXTRA_ID"
        const val EXTRA_TITLE = "com.example.todolistapp.EXTRA_TITLE"
        const val EXTRA_DESCRIPTION = "com.example.todolistapp.EXTRA_DESCRIPTION"
        const val EXTRA_POSITION = "com.example.todolistapp.EXTRA_POSITION"
        const val EXTRA_DUE_DATE = "com.example.todolistapp.EXTRA_DUE_DATE"
    }

    private lateinit var binding: ActivityAddEditTodoBinding
    private var currentTodoId: Long = -1L
    private var currentTodoPosition: Int = -1
    private var currentDueDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentTodoId = intent.getLongExtra(EXTRA_ID, -1L)
        currentTodoPosition = intent.getIntExtra(EXTRA_POSITION, -1)

        if (intent.hasExtra(EXTRA_DUE_DATE)) {
            val dueDateFromIntent = intent.getLongExtra(EXTRA_DUE_DATE, 0L)
            if (dueDateFromIntent != 0L) {
                currentDueDate = dueDateFromIntent
            }
        }

        if (currentTodoId != -1L) {
            binding.editTextTodoTitle.setText(intent.getStringExtra(EXTRA_TITLE))
            binding.editTextTodoDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION))
            supportActionBar?.title = getString(R.string.title_edit_todo)
        } else {
            supportActionBar?.title = getString(R.string.title_add_todo)
        }
        updateDueDateDisplay()

        binding.buttonPickDueDate.setOnClickListener {
            showDateTimePicker()
        }

        binding.buttonSaveTodo.setOnClickListener {
            val title = binding.editTextTodoTitle.text.toString().trim()
            val description = binding.editTextTodoDescription.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, getString(R.string.title_cannot_be_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_TITLE, title)
            resultIntent.putExtra(EXTRA_DESCRIPTION, description)
            currentDueDate?.let { resultIntent.putExtra(EXTRA_DUE_DATE, it) }

            if (currentTodoId != -1L) {
                resultIntent.putExtra(EXTRA_ID, currentTodoId)
                resultIntent.putExtra(EXTRA_POSITION, currentTodoPosition)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Title is set based on add/edit mode in the if/else block above
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun updateDueDateDisplay() {
        if (currentDueDate != null) {
            val sdf = SimpleDateFormat("EEE, MMM dd, yyyy HH:mm", Locale.getDefault())
            binding.textViewDueDate.text = getString(R.string.notification_text_prefix) + sdf.format(currentDueDate)
        } else {
            binding.textViewDueDate.text = getString(R.string.due_date_not_set)
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        if (currentDueDate != null) {
            calendar.timeInMillis = currentDueDate!!
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        currentDueDate = calendar.timeInMillis
                        updateDueDateDisplay()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false // Use true for 24-hour view if preferred
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 // Optional: Set min date to today
        datePickerDialog.show()
    }
}
