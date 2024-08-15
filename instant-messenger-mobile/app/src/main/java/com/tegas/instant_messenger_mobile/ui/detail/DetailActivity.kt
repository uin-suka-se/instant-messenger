package com.tegas.instant_messenger_mobile.ui.detail

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.Result
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.databinding.ActivityDetailBinding
import com.tegas.instant_messenger_mobile.notification.CHANNEL_ID
import com.tegas.instant_messenger_mobile.notification.CHANNEL_NAME
import com.tegas.instant_messenger_mobile.notification.NOTIFICATION_ID
import com.tegas.instant_messenger_mobile.ui.ViewModelFactory
import com.tegas.instant_messenger_mobile.ui.login.LoginActivity
import com.tegas.instant_messenger_mobile.utils.AndroidDownloader
import com.tegas.instant_messenger_mobile.utils.appSettingOpen
import com.tegas.instant_messenger_mobile.utils.isConnected
import com.tegas.instant_messenger_mobile.utils.warningPermissionDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DetailActivity : AppCompatActivity() {

    private val FILE_PICKER_REQUEST_CODE = 1
    private var selectedFilePath: String? = null
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var binding: ActivityDetailBinding
    private lateinit var adapter: MessageAdapter
    private val multiplePermissionId = 14
    private var nim: String = ""
//    private lateinit var chatType: String
    private lateinit var chatId: String
    private val viewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf()
    } else {
        arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }
    private lateinit var snackbar: Snackbar
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
//                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications permission rejected", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var chatType: String

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.d("TEXTWATCHER", s.toString())
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        chatId = intent.getStringExtra("chatId")!!
//        chatType = intent.getStringExtra("chatType")!!
//        val chatName = intent.getStringExtra("chatName")
        Log.d("CHAT ID", "CHAT ID: $chatId")
//        binding.tvName.text = chatName
        val rootView = binding.rootView
        snackbar = Snackbar.make(
            rootView,
            "No Internet Connection",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Setting") {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        binding.ivImage.setOnClickListener {
            if (checkMultiplePermission()) {
                doOperation()
            }
        }
        setRoomChat()
        getSession(chatId)
        fetchData()
        setupSend()
//        setFavoriteButton(item!!)
        setAttachmentButton()
        observeDownload()
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.editText.addTextChangedListener(textWatcher)


    }

    private fun setAttachmentButton() {
        binding.iconAttachment.setOnClickListener {
            openFilePicker()
        }
    }

    private fun observeDownload() {
        viewModel.downloadResponse.observe(this) {
            when (it) {
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    showToast(it.data.message)
                    val message = it.data.message
                    val filePath = it.data.filePath
                    Log.d("DOWNLOAD SUCCESS", it.data.message)
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Log.d("DOWNLOAD ERROR", it.error)
                    showToast(it.error)
                }

                Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    Log.d("DOWNLOAD LOADING", "LOADING")
                }
            }

        }

        viewModel.downloadId.observe(this, Observer { downloadId ->
            Log.d("DownloadViewModel", "Download ID changed: $downloadId")
            Toast.makeText(this, "Download ID changed: $downloadId", Toast.LENGTH_SHORT).show()
            // Update UI or perform actions based on download ID
        })


    }

    private fun setRoomChat() {
//        when (chatType) {
//            "private" -> {
//                binding.tvChatType.text = "Personal Chat"
//            }
//
//            "group" -> {
//                binding.tvChatType.text = "Group Chat"
//            }
//        }
        Glide
            .with(this)
            .load(
                R.drawable
                    .daniela_villarreal
            )
            .into(binding.ivImage)
    }



    private fun setWebSocket(chatId: String?, chatName: String) {
        val intent = Intent(this, WebSocketService::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("nim", nim)
//        intent.putExtra("chatType", chatType)
        startService(intent)
        val uri = URI("ws://192.168.137.1:8181/ws") // Replace with your server IP or hostname
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocket", "Connected to server")
                // Send user ID after connection is open
                val chatID = "{\"id\": \"$chatId\"}" // Replace with appropriate user ID
                send(chatID)

                sendReadReceipt()

            }

            override fun onMessage(message: String?) {
                Log.d("WebSocket", "Received: $message")
                if (message != "Pesan telah dibaca") {
                    val jSOnString = """$message"""
                    val gson = Gson()
                    val messageData: MessagesItem =
                        gson.fromJson(jSOnString, MessagesItem::class.java)
                    adapter.addMessage(messageData)
                    adapter.notifyDataSetChanged()
//
//                    runOnUiThread{
//                        adapter.addMessage(messageData)
//                        binding.rvChat.scrollToPosition(adapter.itemCount - 1)
//                    }

                    Log.d("ADDMESSAGEEEEEEEEEEE", "MESSAGE REFRESHED BY WEBSOCKET")
                    // Handle incoming messages here
                    val sender = messageData.senderId
                    val text = messageData.content
                    val chatId = messageData.chatId

                    Log.d("SENDER NAME", sender)
                    if (messageData.senderId != nim) {
                        sendNotification(sender, text, chatId, chatName)
                    }

                } else if (message == chatId){
                    Log.d("PESAN DITERIMA", message)
                    adapter.updateMessageStateToDelivered()
                }
                else{
                    Log.d("PESAN DIBACA", message)
                    adapter.updateMessageStateToRead()
                }
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

    private fun setFavoriteButton(item: ChatsItem) {
        binding.ivPhone.setOnClickListener {
            viewModel.saveChat(item)
        }

        viewModel.resultFav.observe(this) {
            binding.ivPhone.changeIconColor(R.color.darker_grey)
        }

        viewModel.resultDelFav.observe(this) {
            binding.ivPhone.changeIconColor(R.color.blue)
        }

        viewModel.findFavorite(item?.chatId ?: "") {
            binding.ivImage.changeIconColor(R.color.darker_grey)
        }
    }


    // Function to handle the file picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedFileUri = data?.data ?: return
            selectedFilePath = selectedFileUri.toString()

            // Log the selected file information
            Log.d("SELECTED_FILE", "Selected File Path: $selectedFilePath")

            // You can also log additional details like filename
            val fileName = getFileName(selectedFilePath!!)
            Log.d("SELECTED_FILE", "Filename: $fileName")
        }
        if (selectedFilePath != null) {
            val fileName = selectedFilePath?.substringAfterLast("%2F")
            binding.tvAttacment.visibility = View.VISIBLE
            binding.tvAttacmentName.visibility = View.VISIBLE
            binding.tvAttacmentName.text = fileName
        }
        adapter.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
                    doOperation()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(this)
                    } else {
                        // here warning permission show
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun doOperation() {
        Toast.makeText(
            this,
            "All Permission Granted Successfully!",
            Toast.LENGTH_LONG
        ).show()
        if (isConnected(this)) {
            if (snackbar.isShown) {
                snackbar.dismiss()
            }
            download("http://localhost:5000/download?path=assets/chat%203/alexi_laiho.jpeg")
        } else {
            snackbar.show()
        }
    }

    private fun download(url: String) {
        val folder = File(
            Environment.getExternalStorageDirectory().toString() + "/Download/Images"
        )
        if (!folder.exists()) {
            folder.mkdir()
        }
        showToast("DOWNLOAD STARTED")
        val filename = url.split("/").last()

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )
        request
            .setTitle(filename)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "Image/$filename"
            )
        downloadManager.enqueue(request)
    }

    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    private fun uriToFile(uri: Uri?): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri!!)
        val tempFile = File(cacheDir, "temp_image")
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }

    private fun getSession(chatId: String) {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            binding.rvChat.layoutManager = LinearLayoutManager(this).apply {
                stackFromEnd = true
            }
            viewModel.getChatDetails(chatId, user.nim)
            binding.rvChat.setHasFixedSize(true)
            nim = user.nim


            binding.iconSend.setOnClickListener {
                val senderId = user.nim
                val content = binding.editText.text.toString()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)

                Log.d("CONTENT", "CONTENT: $content")

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                val currentTimeString = dateFormat.format(Date())
                Log.d("TIME IN ACTIVITY", currentTimeString)

                val selectedFile = selectedFilePath?.let {
                    val file = uriToFile(Uri.parse(it)) // Assuming uriToFile converts Uri to File
                    val requestBody = RequestBody.create(
                        "multipart/form-data".toMediaTypeOrNull(),
                        file.readBytes()
                    )
                    MultipartBody.Part.createFormData("attachments", getFileName(it), requestBody)
                }

//                if (selectedFile != null) {
                viewModel.sendMessage(
                    chatId,
                    senderId,
                    content,
                    currentTimeString,
                    selectedFile
                )
//                } else {
//                    // Handle case where no file is selected
//                    Toast.makeText(this, "Please select a file to upload", Toast.LENGTH_SHORT)
//                        .show()
//                }
            }
        }

    }


    private fun setupSend() {
        viewModel.sendMessage.observe(this) {
            when (it) {
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                    Log.e("ERROR", it.error)
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.editText.text?.clear()
                    Toast.makeText(
                        this,
                        "$it.data.messages, status: ${it.data.status}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    // Function to extract filename from the URI
    private fun getFileName(filePath: String): String {
        val uri = Uri.parse(filePath)
        val cursor = contentResolver.query(uri, null, null, null, null)
        return if (cursor != null && cursor.moveToFirst()) {
            val fileName = cursor.getString(0) ?: ""
            cursor.close()
            fileName
        } else {
            "" // Handle case where cursor is null or empty
        }
    }

    private fun fetchData() {
        viewModel.detailViewModel.observe(this) {
            when (it) {
                is Result.Error -> {
                    Log.d("Result", "Error")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${it.error}", Toast.LENGTH_SHORT).show()
                    Log.d("ERROR ACTIVITY", "Error: ${it.error}")
                }

                is Result.Success -> {
                    Log.d("Result", "Success")
                    binding.progressBar.visibility = View.GONE
                    val chatType = it.data.chatType
                    binding.tvChatType.text = if (chatType == "group") {
                        "Group Chat"
                    }else {
                        "Private Chat"
                    }
                    adapter = MessageAdapter(this, viewModel, nim, chatType = chatType)
                    binding.tvName.text = it.data.chatName
                    setWebSocket(chatId, it.data.chatName)
                    binding.rvChat.adapter = adapter
                    adapter.setData(it.data.messages as MutableList<MessagesItem>)
                }

                else -> {
                    Log.d("Result", "Loading")
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }


    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Function to open the file picker intent
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // Accept all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(intent, "Select a file"),
            FILE_PICKER_REQUEST_CODE
        )
    }

    private fun sendNotification(title: String, message: String, chatId: String, chatName: String) {
        Log.d("Notification", "Triggered")
        Log.d("NOTIFICATION", "ChatId: $chatId")
//        Log.d("NOTIFICATION", "ChatType: $chatType")
        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra("chatId", chatId)
//        intent.putExtra("chatType", chatType)
        intent.putExtra("chatName", chatName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.baseline_alarm_on_24)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSubText(getString(R.string.notification_subtext))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            builder.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = builder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_01"
        private const val CHANNEL_NAME = "dicoding channel"
    }

    fun sendReadReceipt() {
        // Membuat objek JSON
        val readReceipt = JSONObject()
        readReceipt.put("chatId", chatId)
        readReceipt.put("read", true)

        // Mengirim objek JSON melalui WebSocket atau proses yang sesuai
        // Contoh:
        // ws.send(readReceipt.toString()) // Mengirimkan sebagai string JSON
        Log.d("READREADREAD", readReceipt.toString())
        webSocketClient.send(readReceipt.toString())
    }
}


fun ImageView.changeIconColor(@ColorRes color: Int) {
    imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this.context, color))
}