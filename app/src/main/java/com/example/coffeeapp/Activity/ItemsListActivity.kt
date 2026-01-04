package com.example.coffeeapp.Activity

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeapp.Adapter.CategoryListItemAdapter
import com.example.coffeeapp.R
import com.example.coffeeapp.ViewModel.MainViewModel
import com.example.coffeeapp.databinding.ActivityItemsListBinding

class ItemsListActivity : AppCompatActivity() {
    lateinit var binding: ActivityItemsListBinding
    private val viewModel = MainViewModel()
    private var id:String = ""
    private var title:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!getBundle()) {
            // If bundle is invalid, finish activity
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        initList()

    }

    private fun initList(){
        binding.apply {
            progressBar.visibility = View.VISIBLE
            viewModel.loadItemCategory(id)
                .observe(this@ItemsListActivity) { items ->
                    if(items.isEmpty()){
                        emptyView.visibility = View.VISIBLE
                        categoryListView.visibility = View.GONE
                        progressBar.visibility = View.GONE
                    } else {
                        categoryListView.layoutManager = LinearLayoutManager(
                            this@ItemsListActivity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        categoryListView.adapter = CategoryListItemAdapter(items)
                        progressBar.visibility = View.GONE
                    }

                }
            backBtn.setOnClickListener {
                finish()
            }
        }
    }

    private fun getBundle(): Boolean{
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!
        //binding.category.text = title
        android.util.Log.d("ItemsListActivity", "Received ID: $id")
        android.util.Log.d("ItemsListActivity", "Received Title: $title")

        if (id.isEmpty()) {
            android.util.Log.e("ItemsListActivity", "ID is empty!")
            return false
        }

        binding.category.text = title
        return true

    }
}