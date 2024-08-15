package com.tegas.instant_messenger_mobile.data.retrofit.response

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

data class ChatDetailResponse(

	@field:SerializedName("messages")
	val messages: List<MessagesItem> = emptyList(),

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("chatId")
	val chatId: String,

	@field:SerializedName("chatName")
	val chatName: String,

	@field:SerializedName("chatType")
	val chatType: String,
)

@Parcelize
data class MessagesItem(

	@field:SerializedName("senderId")
	val senderId: String,

	@field:SerializedName("attachments")
	val attachments: @RawValue Any,

	@field:SerializedName("chatId")
	val chatId: String,

	@field:SerializedName("_id")
	val id: String,

	@field:SerializedName("sentAt")
	val sentAt: String,

	@field:SerializedName("content")
	val content: String,

	@field:SerializedName("senderName")
	val senderName: String,

	var state: Int = 1
): Parcelable {

	fun toJson(): String {
		return Gson().toJson(this)
	}
}
