package com.tegas.instant_messenger_mobile.data.retrofit.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("error")
	val error: Boolean? = null
)

data class Data(

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("nim")
	val nim: String? = null,

	@field:SerializedName("subject")
	val subject: List<String?>? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("department")
	val department: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("chatList")
	val chatList: List<String?>? = null,

	@field:SerializedName("faculty")
	val faculty: String? = null
)
