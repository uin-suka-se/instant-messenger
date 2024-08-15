package com.tegas.instant_messenger_mobile.data.retrofit.response

import com.google.gson.annotations.SerializedName

data class ParticipantResponse(

	@field:SerializedName("participantData")
	val participantData: List<ParticipantDataItem>,

	@field:SerializedName("error")
	val error: Boolean
)

data class ParticipantDataItem(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("userId")
	val userId: String
)
