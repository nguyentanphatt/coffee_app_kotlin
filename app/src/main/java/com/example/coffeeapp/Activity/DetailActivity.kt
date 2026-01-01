package com.example.coffeeapp.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.MainActivity
import com.example.coffeeapp.R
import com.example.coffeeapp.databinding.ActivityDetailBinding
import com.example.project1762.Helper.ManagmentCart

class DetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private lateinit var managementCart: ManagmentCart
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        managementCart= ManagmentCart(this)
        bundle()
        initSizeList()
    }

    private fun bundle(){
        binding.apply {
            item= intent.getSerializableExtra("object") as ItemsModel

            Glide.with(this@DetailActivity)
                .load(item.picUrl[0])
                .into(binding.pictureItem)

            itemDetailTitle.text= item.title
            description.text= item.description
            price.text = "$" + item.price
            rating.text = item.rating.toString()

            addToCartBtn.setOnClickListener {
                item.numberInCart = Integer.valueOf(
                    numOfItem.text.toString()
                )
                managementCart.insertItems(item)
            }

            backBtn.setOnClickListener {
                startActivity(Intent(this@DetailActivity, MainActivity::class.java))
            }

            incBtn.setOnClickListener {
                numOfItem.text = (item.numberInCart + 1).toString()
                item.numberInCart++
            }

            decBtn.setOnClickListener {
                if(item.numberInCart > 1){
                    numOfItem.text = (item.numberInCart - 1).toString()
                    item.numberInCart--
                }
            }
        }


    }

    private fun initSizeList(){
        binding.apply {
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