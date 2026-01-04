package com.example.coffeeapp.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeapp.Adapter.FavoriteAdapter
import com.example.coffeeapp.ViewModel.FavoriteViewModel
import com.example.coffeeapp.databinding.ActivityFavoriteBinding
import com.google.firebase.auth.FirebaseAuth

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var adapter: FavoriteAdapter
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        favoriteViewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]

        checkAuthAndLoadFavorites()
        setupListeners()
        observeViewModels()
    }

    private fun checkAuthAndLoadFavorites() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        userId = currentUser.uid
        initList()
    }

    private fun setupListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun initList() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            favoriteViewModel.loadFavoriteItem(userId)
        }
    }

    private fun observeViewModels() {
        favoriteViewModel.favoriteItem.observe(this) { items ->
            binding.progressBar.visibility = View.GONE

            if (items.isEmpty()) {
                showEmptyState()
            } else {
                showFavoritesList(ArrayList(items))
            }
        }

        favoriteViewModel.operationStatus.observe(this) { status ->
            when (status) {
                is FavoriteViewModel.OperationStatus.Success -> {
                    if (status.message.isNotEmpty()) {
                        Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is FavoriteViewModel.OperationStatus.Error -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                }
                is FavoriteViewModel.OperationStatus.CheckOnly -> {

                }
            }
        }
    }

    private fun showEmptyState() {
        binding.favoriteListView.visibility = View.GONE
        Toast.makeText(this, "No favorites yet", Toast.LENGTH_SHORT).show()
    }

    private fun showFavoritesList(items: ArrayList<com.example.coffeeapp.Domain.ItemsModel>) {
        binding.favoriteListView.visibility = View.VISIBLE

        binding.favoriteListView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        adapter = FavoriteAdapter(
            items,
            userId,
            favoriteViewModel
        )

        binding.favoriteListView.adapter = adapter
        binding.favorite.text = "Favorites (${items.size})"
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            favoriteViewModel.loadFavoriteItem(userId)
        }
    }
}