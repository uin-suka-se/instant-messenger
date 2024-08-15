package com.tegas.instant_messenger_mobile.data.retrofit.response

import com.google.gson.annotations.SerializedName

data class DownloadResponse(

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("filePath")
	val filePath: String? = null
)
