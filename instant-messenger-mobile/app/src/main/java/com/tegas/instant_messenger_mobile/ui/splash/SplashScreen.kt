package com.tegas.instant_messenger_mobile.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.databinding.ActivitySplashScreenBinding
import com.tegas.instant_messenger_mobile.ui.ViewModelFactory
import com.tegas.instant_messenger_mobile.ui.login.LoginActivity
import com.tegas.instant_messenger_mobile.ui.main.MainActivity

class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val viewModel by viewModels<SplashViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        Handler(Looper.getMainLooper()).postDelayed({
            getSesion()
        }, 3000)
    }

    private fun getSesion() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                Log.d("isLogin", "Not login")
                startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
                finish()
            } else {
                Log.d("isLogin", "Login")
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                finish()
            }
        }
    }
}
