package com.example.coffeeapp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.R
import com.example.coffeeapp.ViewModel.CartViewModel
import com.example.coffeeapp.ViewModel.FavoriteViewModel
import com.example.coffeeapp.databinding.ActivityDetailBinding
import com.google.firebase.auth.FirebaseAuth

class DetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel

    private lateinit var cartViewModel: CartViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private var isFavorite = false
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        favoriteViewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        bundle()
        observeViewModels()
        initSizeList()
    }

    private fun observeViewModels() {
        favoriteViewModel.operationStatus.observe(this) { status ->
            when (status) {
                is FavoriteViewModel.OperationStatus.Success -> {
                    isFavorite = status.isFavorite
                    binding.favoriteBtn.isSelected = isFavorite

                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                }
                is FavoriteViewModel.OperationStatus.CheckOnly -> {
                    isFavorite = status.isFavorite
                    binding.favoriteBtn.isSelected = isFavorite
                }
                is FavoriteViewModel.OperationStatus.Error -> {
                    Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bundle() {
        binding.apply {
            item = intent.getSerializableExtra("object") as ItemsModel

            Glide.with(this@DetailActivity)
                .load(item.picUrl[0])
                .into(binding.pictureItem)

            itemDetailTitle.text = item.title
            description.text = item.description
            price.text = "$" + item.price
            rating.text = item.rating.toString()

            val currentUser = auth.currentUser
            if (currentUser != null) {
                favoriteViewModel.checkIsFavorite(currentUser.uid, item.title)
            }

            addToCartBtn.setOnClickListener {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Toast.makeText(
                        this@DetailActivity,
                        "Please login first",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@DetailActivity, LoginActivity::class.java))
                    return@setOnClickListener
                }

                item.numberInCart = numOfItem.text.toString().toInt()
                cartViewModel.addToCart(currentUser.uid, item)

                Toast.makeText(
                    this@DetailActivity,
                    "Added to cart",
                    Toast.LENGTH_SHORT
                ).show()
            }

            backBtn.setOnClickListener {
                finish()
            }

            incBtn.setOnClickListener {
                val currentNum = numOfItem.text.toString().toIntOrNull() ?: 1
                val newNum = currentNum + 1
                numOfItem.text = newNum.toString()
            }

            decBtn.setOnClickListener {
                val currentNum = numOfItem.text.toString().toIntOrNull() ?: 1
                if (currentNum > 1) {
                    val newNum = currentNum - 1
                    numOfItem.text = newNum.toString()
                }
            }

            favoriteBtn.setOnClickListener {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Toast.makeText(
                        this@DetailActivity,
                        "Please login first",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@DetailActivity, LoginActivity::class.java))
                    return@setOnClickListener
                }

                favoriteViewModel.toggleFavorite(currentUser.uid, item)
            }
        }
    }

    private fun initSizeList(){
        binding.apply {
            mediumBtn.setBackgroundResource(R.drawable.dark_brown_stroke)
            smallBtn.setOnClickListener {
                smallBtn.setBackgroundResource(R.drawable.dark_brown_stroke)
                mediumBtn.setBackgroundResource(0)
                largeBtn.setBackgroundResource(0)
            }
            mediumBtn.setOnClickListener {
                mediumBtn.setBackgroundResource(R.drawable.dark_brown_stroke)
                smallBtn.setBackgroundResource(0)
                largeBtn.setBackgroundResource(0)
            }
            largeBtn.setOnClickListener {
                largeBtn.setBackgroundResource(R.drawable.dark_brown_stroke)
                mediumBtn.setBackgroundResource(0)
                smallBtn.setBackgroundResource(0)
            }
        }
    }
}