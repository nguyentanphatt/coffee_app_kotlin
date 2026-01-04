package com.example.coffeeapp.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeapp.Domain.ItemsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.operation.ListenComplete

class FavoriteRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://coffee-app-dcd84-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    fun loadFavoriteItem(userId: String): LiveData<MutableList<ItemsModel>>{
        val listData = MutableLiveData<MutableList<ItemsModel>>()

        val ref = firebaseDatabase.getReference("users")
            .child(userId)
            .child("favorite")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemsModel>()
                for (ds in snapshot.children) {
                    val favoriteItem = ds.getValue(ItemsModel::class.java)
                    if (favoriteItem != null) {
                        list.add(favoriteItem)
                    }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FavoriteRepository", "Firebase error: ${error.message}")
                android.util.Log.e("FavoriteRepository", "Error code: ${error.code}")

                if (error.code == DatabaseError.PERMISSION_DENIED) {
                    android.util.Log.e("FavoriteRepository", "Permission denied - User not authenticated")
                }

                listData.value = mutableListOf()
            }

        })
        return listData
    }

    fun addToFavorite(userId: String, item: ItemsModel, onComplete: (Boolean) -> Unit){
        val ref = firebaseDatabase.getReference("users")
            .child(userId)
            .child("favorite")
            .child(item.title)

        val itemWithTimestamp = item.copy(
            timestamp = System.currentTimeMillis()
        )

        ref.setValue(itemWithTimestamp)
            .addOnSuccessListener {
                android.util.Log.d("FavoriteRepository", "Item added to favorites successfully")
                onComplete(true)
            }
            .addOnFailureListener { error ->
                android.util.Log.e("FavoriteRepository", "Failed to add item to favorites: ${error.message}")
                onComplete(false)
            }
    }

    fun removeFromFavorite(userId: String, itemTitle: String, onComplete: (Boolean) -> Unit) {
        val ref = firebaseDatabase.getReference("users")
            .child(userId)
            .child("favorite")
            .child(itemTitle)

        ref.removeValue()
            .addOnSuccessListener {
                android.util.Log.d("FavoriteRepository", "Item removed from favorites successfully")
                onComplete(true)
            }
            .addOnFailureListener { error ->
                android.util.Log.e("FavoriteRepository", "Failed to remove item from favorites: ${error.message}")
                onComplete(false)
            }
    }

    fun isFavorite(userId: String, itemTitle: String, onResult: (Boolean) -> Unit) {
        val ref = firebaseDatabase.getReference("users")
            .child(userId)
            .child("favorite")
            .child(itemTitle)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onResult(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FavoriteRepository", "Failed to check favorite status: ${error.message}")
                onResult(false)
            }
        })
    }

    fun toggleFavorite(userId: String, item: ItemsModel, onComplete: (Boolean, Boolean) -> Unit) {
        isFavorite(userId, item.title) { isFav ->
            if (isFav) {
                removeFromFavorite(userId, item.title) { success ->
                    onComplete(success, false)
                }
            } else {
                addToFavorite(userId, item) { success ->
                    onComplete(success, true)
                }
            }
        }
    }
}