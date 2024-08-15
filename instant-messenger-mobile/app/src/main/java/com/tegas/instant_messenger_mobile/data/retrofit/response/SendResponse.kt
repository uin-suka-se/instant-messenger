package com.tegas.instant_messenger_mobile.data.retrofit.response

import com.google.gson.annotations.SerializedName

data class SendResponse(

	@field:SerializedName("messages")
	val messages: String? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("status")
	val status: String? = null
)
