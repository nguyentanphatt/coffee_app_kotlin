package com.example.coffeeapp.Helper

import android.content.Context
import android.widget.Toast
import com.example.coffeeapp.Domain.ItemsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ManagementCart(private val context: Context) {

    private val database = FirebaseDatabase.getInstance(
        "https://coffee-app-dcd84-default-rtdb.asia-southeast1.firebasedatabase.app"
    )
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: "guest"
    }

    private fun getCartRef() = database
        .getReference("users")
        .child(getUserId())
        .child("cart")

    private fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun insertItems(item: ItemsModel) {
        if (!isUserLoggedIn()) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val cartItem = hashMapOf(
            "title" to item.title,
            "price" to item.price,
            "picUrl" to item.picUrl,
            "description" to item.description,
            "rating" to item.rating,
            "numberInCart" to item.numberInCart,
            "timestamp" to System.currentTimeMillis()
        )

        getCartRef()
            .child(item.title.replace(" ", "_"))
            .setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getCartItems(callback: (ArrayList<ItemsModel>) -> Unit) {
        if (!isUserLoggedIn()) {
            callback(ArrayList())
            return
        }

        getCartRef().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = ArrayList<ItemsModel>()
                for (child in snapshot.children) {
                    try {
                        val item = ItemsModel(
                            title = child.child("title").getValue(String::class.java) ?: "",
                            price = child.child("price").getValue(Double::class.java) ?: 0.0,
                            picUrl = child.child("picUrl").getValue() as? ArrayList<String> ?: arrayListOf(),
                            description = child.child("description").getValue(String::class.java) ?: "",
                            rating = child.child("rating").getValue(Double::class.java) ?: 0.0,
                            numberInCart = child.child("numberInCart").getValue(Int::class.java) ?: 0
                        )
                        items.add(item)
                    } catch (e: Exception) {
                        android.util.Log.e("ManagementCart", "Error parsing item: ${e.message}")
                    }
                }
                callback(items)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ManagementCart", "Error: ${error.message}")
                callback(ArrayList())
            }
        })
    }

    fun updateQuantity(itemTitle: String, quantity: Int) {
        if (!isUserLoggedIn()) return

        getCartRef()
            .child(itemTitle.replace(" ", "_"))
            .child("numberInCart")
            .setValue(quantity)
    }

    fun removeItem(itemTitle: String, onSuccess: (() -> Unit)? = null) {
        if (!isUserLoggedIn()) return

        getCartRef()
            .child(itemTitle.replace(" ", "_"))
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show()
                onSuccess?.invoke()
            }
    }

    fun clearCart(onSuccess: (() -> Unit)? = null) {
        if (!isUserLoggedIn()) return

        getCartRef().removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Cart cleared", Toast.LENGTH_SHORT).show()
                onSuccess?.invoke()
            }
    }

    fun getTotalPrice(callback: (Double) -> Unit) {
        if (!isUserLoggedIn()) {
            callback(0.0)
            return
        }

        getCartRef().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0.0
                for (child in snapshot.children) {
                    val price = child.child("price").getValue(Double::class.java) ?: 0.0
                    val quantity = child.child("numberInCart").getValue(Int::class.java) ?: 0
                    total += price * quantity
                }
                callback(total)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(0.0)
            }
        })
    }

    fun getCartItemCount(callback: (Int) -> Unit) {
        if (!isUserLoggedIn()) {
            callback(0)
            return
        }

        getCartRef().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (child in snapshot.children) {
                    val quantity = child.child("numberInCart").getValue(Int::class.java) ?: 0
                    count += quantity
                }
                callback(count)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(0)
            }
        })
    }
}