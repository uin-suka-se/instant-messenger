package com.tegas.instant_messenger_mobile.ui.main

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.startActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.Result
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.databinding.ActivityMainBinding
import com.tegas.instant_messenger_mobile.ui.ViewModelFactory
import com.tegas.instant_messenger_mobile.ui.detail.DetailActivity
import com.tegas.instant_messenger_mobile.ui.detail.WebSocketService
import com.tegas.instant_messenger_mobile.ui.login.LoginActivity
import de.hdodenhof.circleimageview.CircleImageView
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> { ViewModelFactory.getInstance(this) }
    private lateinit var nim: String
    private val mList = mutableListOf<ChatsItem>()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var profileCircleImageView: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvNim: TextView
    private lateinit var webSocketClient: WebSocketClient
    private val chatIds = mutableListOf<String>()
    private val adapter by lazy {
        ChatAdapter(this) {
            Intent(this, DetailActivity::class.java).apply {
                putExtra("item", it)
                putExtra("chatId", it.chatId)
                putExtra("chatName", it.name)
                putExtra("chatType", it.chatType)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Instant Messenger"
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.white)))
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.appBarMain.toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        profileCircleImageView = navView.getHeaderView(0).findViewById(R.id.imageView)
        Glide.with(this)
            .load(R.drawable.daniela_villarreal)
            .into(profileCircleImageView)
        tvName = navView.getHeaderView(0).findViewById(R.id.tv_name)
        tvNim = navView.getHeaderView(0).findViewById(R.id.tv_nim)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_logout
            ), drawerLayout
        )

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> {
                    viewModel.logout()
                    true
                }
            }
        }

        observeSession()
        setupRecyclerView()
        fetchData()
        setupLogout()
        setupSearch()
    }

    private fun observeSession() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                nim = user.nim
                tvName.text = user.name
                tvNim.text = nim
//                binding.tvName.text = user.name
                viewModel.getChatList(nim)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.appBarMain.rvUser.layoutManager = LinearLayoutManager(this)
        binding.appBarMain.rvUser.setHasFixedSize(true)
        binding.appBarMain.rvUser.adapter = adapter
    }

    private fun fetchData() {
        viewModel.mainViewModel.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d("Result", "Loading")
                    binding.appBarMain.progressBar.visibility = View.VISIBLE
                }

                is Result.Error -> {
                    Log.d("Result", "Error")
                    binding.appBarMain.progressBar.visibility = View.GONE
                }

                is Result.Success -> {
                    Log.d("Result", "Success")
                    binding.appBarMain.progressBar.visibility = View.GONE
                    mList.clear() // Clear existing list before update
                    mList.addAll(result.data)
                    adapter.setData(mList)

                    Log.d("RESULT DATA", result.data.size.toString())
                    for (i in result.data) {
                        Log.d("First For", i.chatId)
                        Log.d("I.CHATID", i.chatId)
                        // Membuat objek JSON
//                        sendDeliveredReceipt(i.chatId)
                        chatIds.add(i.chatId)
                        setWebSocket(i.chatId)

                    }

//                    for (i in result.data) {
//                        Log.d("Second For", i.chatId)
//                        Log.d("I.CHATID", i.chatId)
//                        // Membuat objek JSON
////                        sendDeliveredReceipt(i.chatId)
//                        chatIds.add(i.chatId)
////                        setWebSocket(i.chatId)
//
//                    }

//                    for (i in chatIds) {
//                        Log.d("Second For", i)
//                        sendDeliveredReceipt(i)
//                    }
                    Log.d("chatIds", chatIds.toString())
                }

                else -> {}
            }
        }
    }

    fun sendDeliveredReceipt(chatId: String) {
        Log.d("sendDeliveredReceipt", "Triggered")
        // Membuat objek JSON
        val readReceipt = JSONObject()
        readReceipt.put("chatId", chatId)
        readReceipt.put("delivered", true)

        // Mengirim objek JSON melalui WebSocket atau proses yang sesuai
        // Contoh:
        // ws.send(readReceipt.toString()) // Mengirimkan sebagai string JSON
        Log.d("DELIVEREDDELIVERED", readReceipt.toString())
        webSocketClient.send(readReceipt.toString())
    }

    private fun setupLogout() {
//        binding.tvLogout.setOnClickListener { viewModel.logout() }
    }

    private fun setupSearch() {
        binding.appBarMain.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            mList.toMutableList()
        } else {
            mList.filter { it.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) }
                .toMutableList()
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }

        adapter.setFilteredList(filteredList)
    }

    private fun onItemClick(item: ChatsItem) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("item", item)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun setWebSocket(chatId: String?) {
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

                for (i in chatIds) {
                    val deliveredReceipt = JSONObject()
                    deliveredReceipt.put("chatId", chatId)
                    deliveredReceipt.put("delivered", true)

                    webSocketClient.send(deliveredReceipt.toString())
                }

            }

            override fun onMessage(message: String?) {
                Log.d("WebSocket", "Received: $message")
                val jSOnString = """$message"""
                val gson = Gson()
                val messageData: MessagesItem = gson.fromJson(jSOnString, MessagesItem::class.java)

                // Find the existing ChatsItem with the same chatId to get the chatType
                val existingChatItem = mList.find { it.chatId == messageData.chatId }

                // If an existing chat item is found, use its chatType
                val chatType = existingChatItem?.chatType ?: ""
                val id = existingChatItem?.id ?: ""
                val participants = existingChatItem?.participants
                val name = existingChatItem?.name ?: ""

                // Create a new ChatsItem from messageData
                val newMessage = ChatsItem(
                    id = id,
                    chatId = messageData.chatId,
                    name = name,
                    lastMessageTime = messageData.sentAt,
                    lastMessage = messageData.content,
                    chatType = chatType,
                    participants = participants!!
                )

                runOnUiThread {
                    // Update the list with the new message
                    val updatedList = mList.map { chatItem ->
                        if (chatItem.chatId == messageData.chatId) newMessage else chatItem
                    }.toMutableList()

                    mList.clear()
                    mList.addAll(updatedList)
                    adapter.setData(mList)
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

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_settings -> {
//                viewModel.logout()
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
