package com.example.coffeeapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.ViewModel.CartViewModel
import com.example.coffeeapp.databinding.ViewholderCartBinding

class CartAdapter(
    private var items: MutableList<ItemsModel>,
    private val userId: String,
    private val viewModel: CartViewModel
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private lateinit var context: Context

    class ViewHolder(val binding: ViewholderCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderCartBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            ItemCartTitle.text = item.title
            ItemCartPrice.text = "$${item.price}"
            ItemCartQuantity.text = item.numberInCart.toString()
            val totalPrice = item.price * item.numberInCart
            ItemCartTotal.text = "$${String.format("%.2f", totalPrice)}"

            // Load image using Glide
            if (item.picUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(item.picUrl[0])
                    .into(ItemCartImg)
            }

            // Increase quantity button
            ItemCartIncBtn.setOnClickListener {
                viewModel.increaseQuantity(userId, item)
            }

            // Decrease quantity button
            ItemCartDecBtn.setOnClickListener {
                if (item.numberInCart > 1) {
                    viewModel.decreaseQuantity(userId, item)
                } else {
                    viewModel.removeFromCart(userId, item.title)
                }
            }

            // Remove item button
            ItemCartRemoveBtn.setOnClickListener {
                viewModel.removeFromCart(userId, item.title)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: ArrayList<ItemsModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}