package com.example.coffeeapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeapp.Activity.DetailActivity
import com.example.coffeeapp.Domain.CategoryModel
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.databinding.ViewholderItemCategory01Binding
import com.example.coffeeapp.databinding.ViewholderItemCategory02Binding

class CategoryListItemAdapter(val items: MutableList<ItemsModel>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        const val TYPE_ITEM1 = 0
        const val TYPE_ITEM2 = 1
    }

    lateinit var context: Context

    //Match into viewholder 1 or 2
    override fun getItemViewType(position: Int): Int {
        return if(position % 2 == 0) TYPE_ITEM1 else TYPE_ITEM2
    }


    class ViewHolderItem01(val binding: ViewholderItemCategory01Binding): RecyclerView.ViewHolder(binding.root)
    class ViewHolderItem02(val binding: ViewholderItemCategory02Binding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        context = parent.context
        return when (viewType){
            TYPE_ITEM1 -> {
                val binding = ViewholderItemCategory01Binding.inflate(LayoutInflater.from(context), parent, false)
                ViewHolderItem01(binding)
            }
            TYPE_ITEM2 -> {
                val binding = ViewholderItemCategory02Binding.inflate(LayoutInflater.from(context), parent, false)
                ViewHolderItem02(binding)
            }
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = items[position]
        fun bindingCommonData (
            title:String,
            price:String,
            rating:Float,
            picture:String
            ){
            when(holder){
                is ViewHolderItem01 -> {
                    holder.binding.categoryPrice01.text = title
                    holder.binding.categoryPrice01.text = price
                    holder.binding.ratingBar01.rating = rating
                    Glide.with(context)
                        .load(picture)
                        .into(holder.binding.categoryItemImg01)

                    holder.itemView.setOnClickListener {
                        val intent = Intent(context, DetailActivity::class.java)
                        intent.putExtra("object", item)
                        context.startActivity(intent)

                    }
                }
                is ViewHolderItem02 -> {
                    holder.binding.categoryTitle02.text = title
                    holder.binding.categoryPrice02.text = price
                    holder.binding.ratingBar02.rating = rating
                    Glide.with(context)
                        .load(picture)
                        .into(holder.binding.categoryItemImg02)

                    holder.itemView.setOnClickListener {
                        val intent = Intent(context, DetailActivity::class.java)
                        intent.putExtra("object", item)
                        context.startActivity(intent)

                    }
                }
            }
        }

        bindingCommonData(
            item.title,
            "${item.price}",
            item.rating.toFloat(),
            item.picUrl[0],
        )
    }

    override fun getItemCount(): Int = items.size

}