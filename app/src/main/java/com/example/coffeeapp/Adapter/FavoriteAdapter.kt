package com.example.coffeeapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeapp.Activity.DetailActivity
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.ViewModel.CartViewModel
import com.example.coffeeapp.ViewModel.FavoriteViewModel
import com.example.coffeeapp.databinding.ViewholderFavoriteBinding

class FavoriteAdapter(
    private var items: ArrayList<ItemsModel>,
    private val userId: String,
    private val favoriteViewModel: FavoriteViewModel
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    private lateinit var context: Context

    class ViewHolder(val binding: ViewholderFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderFavoriteBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {

            favoriteItemTitle.text = item.title
            favoriteItemDescription.text = item.description
            favoriteItemPrice.text = "$${String.format("%.2f", item.price)}"
            favoriteItemRating.text = item.rating.toString()


            if (item.picUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(item.picUrl[0])
                    .into(favoriteItemImg)
            }

            root.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("object", item)
                context.startActivity(intent)
            }

            favoriteRemoveBtn.setOnClickListener {
                favoriteViewModel.removeFromFavorite(userId, item.title)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: ArrayList<ItemsModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}