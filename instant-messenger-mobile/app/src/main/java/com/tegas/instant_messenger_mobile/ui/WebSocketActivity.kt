package com.tegas.instant_messenger_mobile.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.PendingIntentCompat.send
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tegas.instant_messenger_mobile.R
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketActivity : AppCompatActivity() {

    private lateinit var webSocketClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_socket)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        connectWebSocket()
    }

    private fun connectWebSocket() {
        val uri = URI("ws://192.168.137.1:8181/ws") // Replace with your server IP or hostname
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocket", "Connected to server")
                // Send user ID after connection is open
                val chatID = "{\"id\": \"chat 3\"}" // Replace with appropriate user ID
                send(chatID)
            }

            override fun onMessage(message: String?) {
                Log.d("WebSocket", "Received: $message")
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

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}