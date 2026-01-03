package com.example.coffeeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.coffeeapp.Activity.CartActivity
import com.example.coffeeapp.Activity.LoginActivity
import com.example.coffeeapp.Adapter.CategoryAdapter
import com.example.coffeeapp.Adapter.PopularAdapter
import com.example.coffeeapp.ViewModel.MainViewModel
import com.example.coffeeapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        checkAuthAndLoadData()
    }

    private fun checkAuthAndLoadData() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            initBanner()
            initCategory()
            initPopular()
            initBottomNavigation()
        }
    }

    private fun initBanner(){
        binding.progressBarBanner.visibility = View.VISIBLE

        viewModel.loadBanner().observe(this) { banners ->
            if (banners != null && banners.isNotEmpty()) {
                Glide.with(this@MainActivity)
                    .load(banners[0].url)
                    .into(binding.banner)
                binding.progressBarBanner.visibility = View.GONE
            } else {
                binding.progressBarBanner.visibility = View.GONE
            }
        }
    }

    private fun initCategory(){
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.loadCategory().observe(this) { categories ->
            binding.recyclerViewCategory.apply {
                layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = CategoryAdapter(categories)
                minimumHeight = 0
            }
            binding.recyclerViewCategory.minimumHeight = 0
            binding.progressBarCategory.visibility = View.GONE
        }
    }

    private fun initPopular(){
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.loadPopular().observe(this) { items ->
            binding.recyclerViewPopular.apply {
                layoutManager = GridLayoutManager(this@MainActivity, 2)
                adapter = PopularAdapter(items)
                minimumHeight = 0
            }
            binding.recyclerViewPopular.minimumHeight = 0
            binding.progressBarPopular.visibility = View.GONE
        }
    }

    fun initBottomNavigation(){
        binding.cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }
}