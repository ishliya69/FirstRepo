package com.example.todolistapp.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent // Added
import android.content.Context
import android.content.Intent // Added
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todolistapp.MainActivity // Added
import com.example.todolistapp.R

object NotificationUtils {

    const val CHANNEL_ID = "todo_reminders"
    // CHANNEL_NAME is now in strings.xml

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name), // Use string resource
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description) // Use string resource
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildTodoNotification(context: Context, todoId: Long, todoTitle: String): Notification {
        val icon = R.mipmap.ic_launcher

        // Create Intent to launch MainActivity
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            // Optional flags if needed, e.g., to clear task stack
            // flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Optional: If MainActivity should handle this specific item
            // putExtra("TODO_ID_FROM_NOTIFICATION", todoId)
        }

        // Create PendingIntent
        // Using todoId.toInt() as requestCode to make it unique per notification
        val pendingIntent = PendingIntent.getActivity(
            context,
            todoId.toInt(),
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(context.getString(R.string.notification_title)) // Use string resource
            .setContentText(context.getString(R.string.notification_text_prefix) + todoTitle) // Use string resource
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set content intent
            .setAutoCancel(true) // Dismiss notification on tap
            .build()
    }
}
