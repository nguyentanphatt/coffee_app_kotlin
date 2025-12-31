package com.example.coffeeapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.coffeeapp.Adapter.CategoryAdapter
import com.example.coffeeapp.ViewModel.MainViewModel
import com.example.coffeeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBanner()
        initCategory()
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
        viewModel.loadCategory().observeForever {
            binding.recyclerViewCategory.layoutManager =
                LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            binding.recyclerViewCategory.adapter = CategoryAdapter(it)
            binding.recyclerViewCategory.minimumHeight = 0
            binding.progressBarCategory.visibility = View.GONE
        }
    }
}