package com.example.coffeeapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.databinding.ViewholderPopularBinding

class PopularAdapter(val items: MutableList<ItemsModel>): RecyclerView.Adapter<PopularAdapter.ViewHolder>() {
    lateinit var context: Context

    inner class ViewHolder(val binding: ViewholderPopularBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopularAdapter.ViewHolder {
        context = parent.context
        val binding = ViewholderPopularBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularAdapter.ViewHolder, position: Int) {
        holder.binding.titlePop.text = items[position].title
        holder.binding.pricePop.text = "$${items[position].price}"
        Glide.with(context)
            .load(items[position].picUrl[0])
            .into(holder.binding.picture)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}