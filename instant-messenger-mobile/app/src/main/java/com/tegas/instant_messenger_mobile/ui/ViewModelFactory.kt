package com.tegas.instant_messenger_mobile.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tegas.instant_messenger_mobile.data.ChatRepository
import com.tegas.instant_messenger_mobile.data.local.DbModule
import com.tegas.instant_messenger_mobile.ui.Injection.provideRepository
import com.tegas.instant_messenger_mobile.ui.detail.DetailViewModel
import com.tegas.instant_messenger_mobile.ui.login.LoginViewModel
import com.tegas.instant_messenger_mobile.ui.main.MainViewModel
import com.tegas.instant_messenger_mobile.ui.splash.SplashViewModel

class ViewModelFactory(private val repository: ChatRepository, private val db: DbModule): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(repository, db) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                SplashViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    val repository = provideRepository(context)
                    val databaseModule = provideDatabaseModule(context)
                    INSTANCE = ViewModelFactory(repository, databaseModule)
                }
            }
            return INSTANCE as ViewModelFactory
        }

        private fun provideDatabaseModule(context: Context): DbModule {
            return DbModule(context)
        }
    }
}