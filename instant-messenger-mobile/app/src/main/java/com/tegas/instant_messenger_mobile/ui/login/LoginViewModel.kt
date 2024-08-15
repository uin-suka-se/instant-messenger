package com.tegas.instant_messenger_mobile.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.tegas.instant_messenger_mobile.data.ChatRepository
import com.tegas.instant_messenger_mobile.data.Result
import com.tegas.instant_messenger_mobile.data.retrofit.response.LoginResponse

class LoginViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _loginViewModel = MediatorLiveData<Result<LoginResponse>>()
    val loginViewModel: LiveData<Result<LoginResponse>> = _loginViewModel

    fun login(nim: String, password: String) {
        val liveData = repository.login(nim, password)
        _loginViewModel.addSource(liveData) { result ->
            _loginViewModel.value = result
        }
    }
    fun logins(auth: JsonObject) {
        val liveData = repository.logins(auth)
        _loginViewModel.addSource(liveData) { result ->
            _loginViewModel.value = result
        }
    }
}