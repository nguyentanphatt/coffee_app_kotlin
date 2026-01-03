package com.example.coffeeapp.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeapp.Domain.ItemsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://coffee-app-dcd84-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    // Load cart items for a specific user
    fun loadCartItems(userId: String): LiveData<MutableList<ItemsModel>> {
        val listData = MutableLiveData<MutableList<ItemsModel>>()
        val ref = firebaseDatabase.getReference("users").child(userId).child("cart")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemsModel>()
                for (ds in snapshot.children) {
                    val cartItem = ds.getValue(ItemsModel::class.java)
                    if (cartItem != null) {
                        list.add(cartItem)
                    }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("CartRepository", "Firebase error: ${error.message}")
                android.util.Log.e("CartRepository", "Error code: ${error.code}")

                if (error.code == DatabaseError.PERMISSION_DENIED) {
                    android.util.Log.e("CartRepository", "Permission denied - User not authenticated")
                }

                listData.value = mutableListOf()
            }
        })
        return listData
    }

    // Add item to cart
    fun addToCart(userId: String, item: ItemsModel, onComplete: (Boolean) -> Unit) {
        val ref = firebaseDatabase.getReference("users").child(userId).child("cart").child(item.title)

        val itemWithTimestamp = item.copy(
            timestamp = System.currentTimeMillis()
        )

        ref.setValue(itemWithTimestamp)
            .addOnSuccessListener {
                android.util.Log.d("CartRepository", "Item added to cart successfully")
                onComplete(true)
            }
            .addOnFailureListener { error ->
                android.util.Log.e("CartRepository", "Failed to add item to cart: ${error.message}")
                onComplete(false)
            }
    }

    // Remove item from cart
    fun removeFromCart(userId: String, itemTitle: String, onComplete: (Boolean) -> Unit) {
        val ref = firebaseDatabase.getReference("users")
            .child(userId)
            .child("cart")
            .child(itemTitle)

        ref.removeValue()
            .addOnSuccessListener {
                android.util.Log.d("CartRepository", "Item removed from cart successfully")
                onComplete(true)
            }
            .addOnFailureListener { error ->
                android.util.Log.e("CartRepository", "Failed to remove item from cart: ${error.message}")
                onComplete(false)
            }
    }

    // Update item quantity in cart
    fun updateCartItemQuantity(userId: String, itemTitle: String, quantity: Int, onComplete: (Boolean) -> Unit) {
        val ref = firebaseDatabase.getReference("users")
            .child(userId)
            .child("cart")
            .child(itemTitle)
            .child("numberInCart")

        ref.setValue(quantity)
            .addOnSuccessListener {
                android.util.Log.d("CartRepository", "Cart item quantity updated successfully")
                onComplete(true)
            }
            .addOnFailureListener { error ->
                android.util.Log.e("CartRepository", "Failed to update cart item quantity: ${error.message}")
                onComplete(false)
            }
    }

    // Clear all items from cart
    fun clearCart(userId: String, onComplete: (Boolean) -> Unit) {
        val ref = firebaseDatabase.getReference("users").child(userId).child("cart")

        ref.removeValue()
            .addOnSuccessListener {
                android.util.Log.d("CartRepository", "Cart cleared successfully")
                onComplete(true)
            }
            .addOnFailureListener { error ->
                android.util.Log.e("CartRepository", "Failed to clear cart: ${error.message}")
                onComplete(false)
            }
    }
}