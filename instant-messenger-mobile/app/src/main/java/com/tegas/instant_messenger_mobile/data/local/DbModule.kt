package com.tegas.instant_messenger_mobile.data.local

import android.content.Context
import androidx.room.Room

class DbModule(private val ctx: Context) {
private val db = Room.databaseBuilder(ctx, ChatDb::class.java, "chats.db")
    .allowMainThreadQueries()
    .build()

    val chatDao = db.chatDao()
}