package com.example.todolistapp

import android.Manifest // Added
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager // Added
import android.os.Build // Added
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat // Added
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.databinding.ActivityMainBinding
import com.example.todolistapp.util.NotificationScheduler // Ensure this import is present
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var addTodoActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var editTodoActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String> // Added
    private lateinit var todoDao: TodoDao

    private val PREFS_NAME = "TodoAppPrefs"
    private val PREF_SORT_BY = "sortBy"
    private val PREF_SORT_ASC = "sortAsc"
    private val PREF_FILTER_STATUS = "filterStatus"

    private lateinit var sharedPreferences: SharedPreferences
    private var currentSortBy: String = "createdAt"
    private var currentSortAsc: Boolean = false
    private var currentFilter: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize notification permission launcher
        notificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Toast.makeText(this, getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.notification_permission_denied), Toast.LENGTH_LONG).show()
                }
            }

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadPreferences()

        todoDao = AppDatabase.getDatabase(applicationContext).todoDao()

        // Request notification permission
        checkAndRequestNotificationPermission() // Added call

        addTodoActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val title = data?.getStringExtra(AddEditTodoActivity.EXTRA_TITLE)
                val description = data?.getStringExtra(AddEditTodoActivity.EXTRA_DESCRIPTION)
                val dueDate = data?.getLongExtra(AddEditTodoActivity.EXTRA_DUE_DATE, 0L)?.takeIf { it != 0L }

                if (title != null) {
                    val newTodoItem = TodoItem(
                        id = 0L,
                        title = title,
                        description = description,
                        isCompleted = false,
                        createdAt = System.currentTimeMillis(),
                        dueDate = dueDate
                    )
                    lifecycleScope.launch {
                        val generatedId = todoDao.insert(newTodoItem)
                        val scheduledTodoItem = newTodoItem.copy(id = generatedId)
                        if (scheduledTodoItem.dueDate != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                if (!alarmManager.canScheduleExactAlarms()) {
                                    Toast.makeText(this@MainActivity, getString(R.string.grant_exact_alarm_permission), Toast.LENGTH_LONG).show()
                                    // Optionally, direct user to settings:
                                    // Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { startActivity(it) }
                                }
                            }
                            NotificationScheduler.scheduleNotification(this@MainActivity, scheduledTodoItem)
                        }
                    }
                    Toast.makeText(this, getString(R.string.item_added, title), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.could_not_add_item_title_missing), Toast.LENGTH_LONG).show()
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.add_item_cancelled), Toast.LENGTH_SHORT).show()
            }
        }

        editTodoActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val id = data?.getLongExtra(AddEditTodoActivity.EXTRA_ID, -1L)
                val title = data?.getStringExtra(AddEditTodoActivity.EXTRA_TITLE)
                val description = data?.getStringExtra(AddEditTodoActivity.EXTRA_DESCRIPTION)
                val dueDate = data?.getLongExtra(AddEditTodoActivity.EXTRA_DUE_DATE, 0L)?.takeIf { it != 0L }

                if (id != -1L && title != null) {
                    lifecycleScope.launch {
                        val originalItem = todoDao.getTodoItemById(id)
                        if (originalItem != null) {
                            val updatedTodoItem = originalItem.copy(
                                title = title,
                                description = description,
                                dueDate = dueDate
                            )
                            todoDao.update(updatedTodoItem)
                            NotificationScheduler.cancelNotification(this@MainActivity, updatedTodoItem.id)
                            if (updatedTodoItem.dueDate != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                    if (!alarmManager.canScheduleExactAlarms()) {
                                        Toast.makeText(this@MainActivity, getString(R.string.grant_exact_alarm_permission), Toast.LENGTH_LONG).show()
                                    }
                                }
                                NotificationScheduler.scheduleNotification(this@MainActivity, updatedTodoItem)
                            }
                            Toast.makeText(this@MainActivity, getString(R.string.item_updated, title), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Error updating item: Item not found", Toast.LENGTH_LONG).show() // Keep as is or add to strings
                        }
                    }
                } else {
                    Toast.makeText(this, "Error updating item: Missing data", Toast.LENGTH_LONG).show() // Keep as is or add to strings
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.edit_cancelled), Toast.LENGTH_SHORT).show()
            }
        }

        todoAdapter = TodoAdapter(mutableListOf())
        todoAdapter.onItemClickListener = this
        todoAdapter.onItemCheckedChanged = { todoItem ->
            lifecycleScope.launch {
                todoDao.update(todoItem)
                if (todoItem.isCompleted || todoItem.dueDate == null) {
                    NotificationScheduler.cancelNotification(this@MainActivity, todoItem.id)
                } else {
                    // Item is not completed and has a due date, so schedule/reschedule
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            Toast.makeText(this@MainActivity, "Please grant permission to schedule exact alarms for reminders to work reliably.", Toast.LENGTH_LONG).show()
                            // Optionally, direct user to settings
                        }
                    }
                    NotificationScheduler.scheduleNotification(this@MainActivity, todoItem)
                }
            }
        }
        binding.recyclerViewTodoItems.adapter = todoAdapter
        binding.recyclerViewTodoItems.layoutManager = LinearLayoutManager(this)

        observeTodoList()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val itemToDelete = todoAdapter.getItemAt(position)
                    if (itemToDelete != null) {
                        lifecycleScope.launch {
                            todoDao.delete(itemToDelete)
                        }
                        NotificationScheduler.cancelNotification(this@MainActivity, itemToDelete.id)
                        Toast.makeText(this@MainActivity, getString(R.string.item_deleted, itemToDelete.title), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTodoItems)

        binding.fabAddTodo.setOnClickListener {
            val intent = Intent(this, AddEditTodoActivity::class.java)
            addTodoActivityLauncher.launch(intent)
        }
    }

    override fun onItemClick(todoItem: TodoItem, position: Int) {
        val intent = Intent(this, AddEditTodoActivity::class.java).apply {
            putExtra(AddEditTodoActivity.EXTRA_ID, todoItem.id)
            putExtra(AddEditTodoActivity.EXTRA_TITLE, todoItem.title)
            putExtra(AddEditTodoActivity.EXTRA_DESCRIPTION, todoItem.description)
            putExtra(AddEditTodoActivity.EXTRA_POSITION, position)
            todoItem.dueDate?.let { putExtra(AddEditTodoActivity.EXTRA_DUE_DATE, it) }
        }
        editTodoActivityLauncher.launch(intent)
    }

    private fun loadPreferences() {
        currentSortBy = sharedPreferences.getString(PREF_SORT_BY, "createdAt") ?: "createdAt"
        currentSortAsc = sharedPreferences.getBoolean(PREF_SORT_ASC, false)
        val filterValue = sharedPreferences.getInt(PREF_FILTER_STATUS, -1)
        currentFilter = when(filterValue) {
            0 -> false
            1 -> true
            else -> null
        }
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putString(PREF_SORT_BY, currentSortBy)
        editor.putBoolean(PREF_SORT_ASC, currentSortAsc)
        val filterValue = when(currentFilter) {
            false -> 0
            true -> 1
            null -> -1
        }
        editor.putInt(PREF_FILTER_STATUS, filterValue)
        editor.apply()
    }

    private fun observeTodoList() {
        lifecycleScope.launch {
            val flow = when (currentSortBy) {
                "title" -> if (currentSortAsc) todoDao.getAllTodoItemsByTitleAsc(currentFilter) else todoDao.getAllTodoItemsByTitleDesc(currentFilter)
                "createdAt" -> if (currentSortAsc) todoDao.getAllTodoItemsByDateAsc(currentFilter) else todoDao.getAllTodoItemsByDateDesc(currentFilter)
                "isCompleted" -> if (currentSortAsc) todoDao.getAllTodoItemsByCompletionStatusAsc(currentFilter) else todoDao.getAllTodoItemsByCompletionStatusDesc(currentFilter)
                else -> todoDao.getAllTodoItemsByDateDesc(currentFilter)
            }
            flow.collect { items ->
                todoAdapter.updateItems(items)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        when (currentFilter) {
            null -> menu.findItem(R.id.action_filter_all)?.isChecked = true
            false -> menu.findItem(R.id.action_filter_active)?.isChecked = true
            true -> menu.findItem(R.id.action_filter_completed)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var sortChanged = false
        var filterChanged = false

        when (item.itemId) {
            R.id.action_sort_title_asc -> {
                if (currentSortBy != "title" || !currentSortAsc) {
                    currentSortBy = "title"; currentSortAsc = true; sortChanged = true
                }
            }
            R.id.action_sort_title_desc -> {
                if (currentSortBy != "title" || currentSortAsc) {
                    currentSortBy = "title"; currentSortAsc = false; sortChanged = true
                }
            }
            R.id.action_sort_date_desc -> {
                if (currentSortBy != "createdAt" || currentSortAsc) {
                    currentSortBy = "createdAt"; currentSortAsc = false; sortChanged = true
                }
            }
            R.id.action_sort_date_asc -> {
                if (currentSortBy != "createdAt" || !currentSortAsc) {
                    currentSortBy = "createdAt"; currentSortAsc = true; sortChanged = true
                }
            }
            R.id.action_sort_status_asc -> {
                if (currentSortBy != "isCompleted" || !currentSortAsc) {
                    currentSortBy = "isCompleted"; currentSortAsc = true; sortChanged = true
                }
            }
            R.id.action_sort_status_desc -> {
                if (currentSortBy != "isCompleted" || currentSortAsc) {
                    currentSortBy = "isCompleted"; currentSortAsc = false; sortChanged = true
                }
            }
            R.id.action_filter_all -> {
                if (currentFilter != null) {
                    currentFilter = null; filterChanged = true; item.isChecked = true
                }
            }
            R.id.action_filter_active -> {
                if (currentFilter != false) {
                    currentFilter = false; filterChanged = true; item.isChecked = true
                }
            }
            R.id.action_filter_completed -> {
                if (currentFilter != true) {
                    currentFilter = true; filterChanged = true; item.isChecked = true
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }

        if (sortChanged || filterChanged) {
            savePreferences()
            observeTodoList()
        }
        return true
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    // Toast.makeText(this, "Notification permission already available.", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show an educational UI to the user explaining why the permission is needed
                    // For now, just request it. In a real app, you'd show a dialog.
                    // Consider creating a dialog here to explain.
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly request the permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        // For SDK < Tiramisu, no runtime permission is needed for POST_NOTIFICATIONS
    }
}
