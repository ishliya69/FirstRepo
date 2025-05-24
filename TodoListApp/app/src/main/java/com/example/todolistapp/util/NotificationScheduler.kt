package com.example.todolistapp.util

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.todolistapp.TodoItem
import com.example.todolistapp.TodoNotificationReceiver

object NotificationScheduler {

    fun scheduleNotification(context: Context, todoItem: TodoItem) {
        if (todoItem.dueDate == null || todoItem.dueDate <= System.currentTimeMillis()) {
            // Do not schedule if no due date or if it's in the past
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TodoNotificationReceiver::class.java).apply {
            putExtra(TodoNotificationReceiver.EXTRA_TODO_ID, todoItem.id)
            putExtra(TodoNotificationReceiver.EXTRA_TODO_TITLE, todoItem.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todoItem.id.toInt(), // Use TodoItem's ID as the request code for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, todoItem.dueDate, pendingIntent)
                } else {
                    // Fallback or request permission. For now, just log.
                    // In a real app, you'd guide the user to settings or use an inexact alarm.
                    Log.w("NotificationScheduler", "Cannot schedule exact alarms. Consider requesting SCHEDULE_EXACT_ALARM permission or using inexact alarms.")
                    // Optionally, schedule an inexact alarm as a fallback:
                    // alarmManager.setWindow(AlarmManager.RTC_WAKEUP, todoItem.dueDate, 60000, pendingIntent) // e.g., 1 min window
                }
            } else {
                // For older versions, setExactAndAllowWhileIdle is available without special permission
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, todoItem.dueDate, pendingIntent)
            }
        } catch (e: SecurityException) {
            // This might happen if trying to set an exact alarm without permission on some devices/versions
            Log.e("NotificationScheduler", "SecurityException while scheduling alarm for item ${todoItem.id}", e)
            // Consider a fallback to an inexact alarm or user notification about the issue
        }
    }

    fun cancelNotification(context: Context, todoId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TodoNotificationReceiver::class.java) // Intent must match the one used for scheduling

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            todoId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
