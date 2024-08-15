package com.tegas.instant_messenger_mobile.ui.favorite

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tegas.instant_messenger_mobile.data.local.DbModule
import com.tegas.instant_messenger_mobile.databinding.ActivityFavoriteBinding
import com.tegas.instant_messenger_mobile.ui.detail.DetailActivity
import com.tegas.instant_messenger_mobile.ui.main.ChatAdapter

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private val adapter by lazy {
        ChatAdapter(this) {
            Intent(this, DetailActivity::class.java).apply {
                putExtra("item", it)
                startActivity(this)
            }
        }
    }

    private val viewModel by viewModels<FavoriteViewModel> {
        FavoriteViewModel.Factory(DbModule(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.rvUser.layoutManager = LinearLayoutManager(this)
        binding.rvUser.adapter = adapter

        binding.ivLines.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.getFavoriteUser().observe(this) {
            adapter.setData(it)
        }
    }
}