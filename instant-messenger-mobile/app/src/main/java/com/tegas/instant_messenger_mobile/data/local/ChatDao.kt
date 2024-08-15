package com.tegas.instant_messenger_mobile.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem

@Dao
interface ChatDao {

    @Upsert
    fun insert(chat: ChatsItem)

    @Query("SELECT * FROM chats")
    fun loadAll(): LiveData<MutableList<ChatsItem>>

    @Query("SELECT * FROM chats WHERE id = :id LIMIT 1")
    fun findById(id: String): ChatsItem

    @Delete
    fun delete(chat: ChatsItem)
}