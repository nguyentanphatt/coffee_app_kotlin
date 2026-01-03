package com.example.coffeeapp.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeapp.Activity.ItemsListActivity
import com.example.coffeeapp.Domain.CategoryModel
import com.example.coffeeapp.R
import com.example.coffeeapp.databinding.ViewholderCategoryBinding

class CategoryAdapter (val items: MutableList<CategoryModel>): RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var seletedPosition = -1
    private var lastSelectedPosition = -1

    inner class ViewHolder(val binding: ViewholderCategoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryAdapter.ViewHolder {
        context = parent.context
        val binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = items[position]
        holder.binding.titleCategory.text = item.title
        holder.binding.root.setOnClickListener {
            lastSelectedPosition=seletedPosition
            seletedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(seletedPosition)

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(context, ItemsListActivity::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("title", item.title)

                }
                ContextCompat.startActivity(context, intent, null)
            }, 500)
        }
        if(seletedPosition == position){
            holder.binding.titleCategory.setBackgroundResource(R.drawable.dark_brown_bg)
            holder.binding.titleCategory.setTextColor(context.resources.getColor(R.color.white))
        } else {
            holder.binding.titleCategory.setBackgroundResource(R.drawable.white_bg)
            holder.binding.titleCategory.setTextColor(context.resources.getColor(R.color.darkBrown))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}