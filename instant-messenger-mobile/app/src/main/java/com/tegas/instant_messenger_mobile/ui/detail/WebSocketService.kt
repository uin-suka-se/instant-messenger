package com.tegas.instant_messenger_mobile.ui.detail

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.activity.viewModels
import com.google.gson.Gson
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.ui.ViewModelFactory
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketService : Service() {

    private lateinit var webSocketClient: WebSocketClient
    private var chatId: String? = null
    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        chatId = intent?.getStringExtra("chatId")
        startWebSocket()
        return START_STICKY
    }

    private fun startWebSocket() {
        val uri = URI("ws://192.168.137.1:8181/ws")
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocket", "Connected to server")
                chatId?.let {
                    val chatID = "{\"id\": \"$it\"}"
                    send(chatID)
                }
            }

            override fun onMessage(message: String?) {
                Log.d("WebSocket", "Received: $message")
                val jSOnString = """$message"""
                val gson = Gson()
                val messageData: MessagesItem = gson.fromJson(jSOnString, MessagesItem::class.java)
                Log.d("Websocket Message Out", messageData.toString())
                Log.d("ADDMESSAGEEEEEEEEEEE", "MESSAGE REFRESHED BY WEBSOCKET")
                // Handle incoming messages here

            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("WebSocket", "Connection closed")
            }

            override fun onError(ex: Exception?) {
                Log.e("WebSocket", "Error: ${ex?.message}")
            }
        }
        webSocketClient.connect()
    }

    private fun startForegroundService() {
        val notificationId = 1
        val channelId = "websocket_service_channel"
        val channelName = "WebSocket Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("WebSocket Service")
            .setContentText("WebSocket connection is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(notificationId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}
