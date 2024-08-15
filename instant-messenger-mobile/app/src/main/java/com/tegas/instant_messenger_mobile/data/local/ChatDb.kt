package com.tegas.instant_messenger_mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem

@Database(entities = [ChatsItem::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class ChatDb: RoomDatabase() {
    abstract fun chatDao(): ChatDao
}