package com.example.todolistapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todolistapp.util.NotificationUtils

class TodoNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TODO_ID = "com.example.todolistapp.EXTRA_TODO_ID"
        const val EXTRA_TODO_TITLE = "com.example.todolistapp.EXTRA_TODO_TITLE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        val todoTitle = intent.getStringExtra(EXTRA_TODO_TITLE) ?: "Task Reminder"

        if (todoId == -1L) {
            // Invalid ID, do nothing
            return
        }

        // Ensure notification channel is created (important for Android O+)
        NotificationUtils.createNotificationChannel(context)

        val notification = NotificationUtils.buildTodoNotification(context, todoId, todoTitle)

        // Check for POST_NOTIFICATIONS permission before attempting to notify
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(todoId.toInt(), notification)
        } else {
            // TODO: Handle the case where permission is not granted.
            // For now, it simply won't show the notification.
            // A real app might re-request permission or inform the user.
            println("Notification permission not granted for Todo ID: $todoId")
        }
    }
}
