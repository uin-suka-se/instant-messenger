package com.tegas.instant_messenger_mobile.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.ui.detail.DetailActivity
import okhttp3.internal.concurrent.Task

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val messageDataJson = inputData.getString("messageData")
        val messageData = Gson().fromJson(messageDataJson, MessagesItem::class.java)

        // Perform background tasks such as showing notifications
        showNotification(messageData)

        return Result.success()
    }

    private fun showNotification(messageData: MessagesItem) {
        // Example: Create and show a notification using NotificationCompat
        // Replace with your actual notification logic
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Build your notification here
        // NotificationCompat.Builder can be used to build a notification
    }
}