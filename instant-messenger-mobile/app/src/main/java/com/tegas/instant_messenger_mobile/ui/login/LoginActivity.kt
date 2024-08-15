package com.tegas.instant_messenger_mobile.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tegas.instant_messenger_mobile.R
import com.tegas.instant_messenger_mobile.data.Result
import com.tegas.instant_messenger_mobile.databinding.ActivityLoginBinding
import com.tegas.instant_messenger_mobile.ui.ViewModelFactory
import com.tegas.instant_messenger_mobile.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

//        login()
//        setupLogin()

        login()
        viewModel.loginViewModel
        setupLogin()
    }

    private fun setupLogin() {

        binding.btnLogin.setOnClickListener {
            val nim = binding.etNim.text.toString()
            val password = binding.etPassword.text.toString()
            val auth = """{
                |"nim": "$nim",
                |"password": $password
                |}""".trimMargin()
            val authJson = JsonParser.parseString(auth).asJsonObject
            val jSon = JsonObject().apply {
                addProperty("nim", nim)
                addProperty("password", password)
            }

            viewModel.logins(jSon)
        }
    }

    private fun login() {
        viewModel.loginViewModel.observe(this) {
            when (it) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val nim = it.data.data?.nim
                    val name = it.data.data?.name
                    Log.d("NIM", "NIM: $nim")
                    AlertDialog.Builder(this).apply {
                        setTitle("Hello")
                        setMessage("$name $nim")
                        setPositiveButton("Okay") { _, _ ->
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("nim", nim)
                            startActivity(intent)
                        }
                        create()
                        show()
                    }
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}
