package com.tegas.instant_messenger_mobile.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tegas.instant_messenger_mobile.data.pref.UserPreference
import com.tegas.instant_messenger_mobile.data.retrofit.ApiService
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatDetailResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.ChatsItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.DownloadResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.LoginResponse
import com.tegas.instant_messenger_mobile.data.retrofit.response.MessagesItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.ParticipantDataItem
import com.tegas.instant_messenger_mobile.data.retrofit.response.SendResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ChatRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    fun getChatList(nim: String): LiveData<Result<List<ChatsItem>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.getChatList(nim)
                val chats = response.chats
                emit(Result.Success(chats))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun getChatDetails(chatId: String, userId: String): LiveData<Result<ChatDetailResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                Log.d("REPOSITORY", "REPOSITORY CHAT ID: $chatId")
                val response = apiService.getChatDetails(chatId, userId)
                val item = response
                emit(Result.Success(item))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun getParticipant(chatId: String): LiveData<Result<List<ParticipantDataItem>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                Log.d("REPOSITORY PARticipant", "REPOSITORY CHAT ID: $chatId")
                val response = apiService.getParticipants(chatId)
                val participants = response.participantData
                emit(Result.Success(participants))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun downloadFileWithManager(url: String): LiveData<Result<DownloadResponse>> {
        val liveData = MutableLiveData<Result<DownloadResponse>>()
        liveData.value = Result.Loading

        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("File Download")
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getFileNameFromUrl(url))

            val downloadManager = getSystemService(context, DownloadManager::class.java)
            val downloadId = downloadManager!!.enqueue(request)

            // Query the download status
            val query = DownloadManager.Query().setFilterById(downloadId)
            var downloading = true
            while (downloading) {
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(columnIndex)
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloading = false
                            val filePathIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val filePath = cursor.getString(filePathIndex)
                            liveData.postValue(Result.Success(DownloadResponse(error = false, message = "File downloaded successfully", filePath = filePath)))
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloading = false
                            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val errorMessage = "Download failed: ${cursor.getInt(reasonIndex)}"
                            liveData.postValue(Result.Error(errorMessage))
                        }
                        // Handle other status codes as needed
                        else -> {
                            // Continue downloading or handle other statuses
                        }
                    }
                } else {
                    liveData.postValue(Result.Error("Download query returned empty cursor or couldn't move to first"))
                    downloading = false
                }
                cursor?.close()
                Thread.sleep(1000) // Optional delay to throttle query frequency
            }
        } catch (e: Exception) {
            liveData.postValue(Result.Error(e.message.toString()))
        }

        return liveData
    }

    fun downloadFile(path: String): LiveData<Result<DownloadResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.downloadFile(path)
                if (response.isSuccessful) {
                    val contentType = response.headers()["Content-Type"]
                    if (contentType != null && contentType.startsWith("application/json")) {
                        val responseBody = response.body()?.string()
                        responseBody?.let {
                            val errorResponse = Gson().fromJson(it, DownloadResponse::class.java)
                            emit(Result.Error(errorResponse.message))
                            Log.d("DOWNLOAD ERROR MESSAGE", errorResponse.message)
                        } ?: run {
                            emit(Result.Error("Unknown error"))
                        }
                    } else {
                        response.body()?.let { responseBody ->
                            val fileName = getFileNameFromUrl(path)
                            val savedFilePath = saveFile(responseBody, fileName)
                            emit(
                                Result.Success(
                                    DownloadResponse(
                                        error = false,
                                        message = "File downloaded successfully",
                                        filePath = savedFilePath
                                    )
                                )
                            )
                        }

                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    emit(Result.Error(errorMessage))
                    Log.d("DOWNLOAD ERROR MESSAGE", errorMessage)
                }
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
                Log.d("DOWNLOAD ERROR MESSAGE", e.message.toString())
            }
        }

    private fun getFileNameFromUrl(url: String): String {
        return url.substring(url.lastIndexOf('/') + 1)
    }

    private fun saveFile(body: ResponseBody, fileName: String): String {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = body.byteStream()
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }


    private fun writeFile(data: ByteArray, file: File) {
        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(data)
            outputStream.close()
        } catch (e: IOException) {
            Log.e("DOWNLOAD", "Error writing file: $e")
        }
    }

    fun login(
        nim: String,
        password: String
    ): LiveData<Result<LoginResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.login(nim, password)
                val nim = response.data?.nim
                val name = response.data?.name
                Log.d("LOGIN SUCCESS", "Name: $name, NIM: $nim")
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun logins(auth: JsonObject): LiveData<Result<LoginResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.logins(auth)
                val data = response.data
                val error = response.error
                val name = response.data?.name
                val nim = response.data?.nim
                saveSession(UserModel(name!!, nim!!))
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun sendMessage(
        chatId: String,
        senderId: String,
        content: String,
        sentAt: String,
        attachments: MultipartBody.Part?
    ): LiveData<Result<SendResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                Log.d("SELECTED FILE IN REPOSITORY", attachments.toString())
                Log.d("TIME IN REPOSITORY TRY", sentAt)
                val data = mapOf(
                    "chatId" to RequestBody.create(MultipartBody.FORM, chatId),
                    "senderId" to RequestBody.create(MultipartBody.FORM, senderId),
                    "content" to RequestBody.create(MultipartBody.FORM, content),
                    "sentAt" to RequestBody.create(MultipartBody.FORM, sentAt),
                )
                val response =
                    if (attachments != null) {
                        apiService.sendMessage(data, attachments)
                    } else {
                        apiService.sendMessage(data)
                    }
                Log.d("Success", response.messages.toString())
                emit(Result.Success(response))
            } catch (e: Exception) {
                Log.d("SELECTED FILE IN REPOSITORY", attachments.toString())
                Log.d("TIME IN REPOSITORY CATCH", sentAt)
                emit(Result.Error(e.message.toString()))
            }
        }

    private suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var instance: ChatRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference,
            context: Context
        ): ChatRepository =
            instance ?: synchronized(this) {
                instance ?: ChatRepository(context, apiService, userPreference)
            }.also { instance = it }
    }
}