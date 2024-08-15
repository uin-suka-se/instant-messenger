package com.tegas.instant_messenger_mobile.data.retrofit.response

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ChatListResponse(

	@field:SerializedName("chats")
	val chats: List<ChatsItem> = emptyList(),

	@field:SerializedName("error")
	val error: Boolean
)

@Parcelize
@Entity(tableName = "chats")
data class ChatsItem(

	@PrimaryKey
	@field:SerializedName("chatId")
	val chatId: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("lastMessage")
	val lastMessage: String,

	@field:SerializedName("lastMessageTime")
	val lastMessageTime: String,

	@field:SerializedName("_id")
	val id: String,

	@field:SerializedName("participants")
	val participants: List<String>,

	@field:SerializedName("chatType")
	val chatType: String
): Parcelable
