package com.tegas.instant_messenger_mobile.data

data class UserModel(
    val name: String,
    val nim: String,
    val isLogin: Boolean = false
)
