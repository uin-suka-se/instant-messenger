package com.tegas.instant_messenger_mobile.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tegas.instant_messenger_mobile.data.local.DbModule

class FavoriteViewModel(private val dbModule: DbModule): ViewModel() {

    fun getFavoriteUser() = dbModule.chatDao.loadAll()

    class Factory(private val dbModule: DbModule): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoriteViewModel(dbModule) as T
        }
    }
}