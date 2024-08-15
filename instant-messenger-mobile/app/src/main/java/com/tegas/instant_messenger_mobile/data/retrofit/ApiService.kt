package com.tegas.instant_messenger_mobile.data.retrofit

import com.google.gson.JsonObject
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatDetailResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatListResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.DownloadResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.LoginResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.ParticipantResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.SendResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.util.Date

interface ApiService {

    @GET("chatList")
    suspend fun getChatList(
        @Query("nim") nim: String
    ): ChatListResponse

    @GET("chatDetail")
    fun getChatDetail(
        @Query("chatId") chatId: String
    ): Call<ChatDetailResponse>

    @GET("chatDetail")
    suspend fun getChatDetails(
        @Query("chatId") chatId: String,
        @Query("userId") userId: String
    ): ChatDetailResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("login")
    suspend fun logins(
        @Body auth: JsonObject
    ): LoginResponse

    @Multipart
    @POST("messages")
    suspend fun sendMessage(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part attachments: MultipartBody.Part? = null
    ): SendResponse

    @GET("participant")
    suspend fun getParticipants(
        @Query("chatId") chatId: String
    ): ParticipantResponse

    @GET("download")
    @Streaming
    suspend fun downloadFile(
        @Query("path") path: String
    ): retrofit2.Response<ResponseBody>
}