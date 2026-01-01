package com.example.coffeeapp.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeapp.Domain.BannerModel
import com.example.coffeeapp.Domain.CategoryModel
import com.example.coffeeapp.Domain.ItemsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class MainRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://coffee-app-dcd84-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    fun loadBanner(): LiveData<MutableList<BannerModel>>{
        val listData = MutableLiveData<MutableList<BannerModel>>()
        val ref = firebaseDatabase.getReference("Banner")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerModel>()
                for(ds in snapshot.children){
                    val banner = ds.getValue(BannerModel::class.java)
                    if(banner!=null){
                        list.add(banner)
                    }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("MainRepository", "Firebase error: ${error.message}")
            }

        })
        return listData
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for(ds in snapshot.children){
                    val category = ds.getValue(CategoryModel::class.java)
                    if(category!=null){
                        list.add(category)
                    }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("MainRepository", "Firebase error: ${error.message}")
            }

        })
        return listData
    }

    fun loadPopular(): LiveData<MutableList<ItemsModel>>{
        val listData = MutableLiveData<MutableList<ItemsModel>>()
        val ref = firebaseDatabase.getReference("Popular")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemsModel>()
                for(ds in snapshot.children){
                    val popular = ds.getValue(ItemsModel::class.java)
                    if(popular!=null){
                        list.add(popular)
                    }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("MainRepository", "Firebase error: ${error.message}")
            }

        })
        return listData
    }

    fun loadItemCategory(categoryId:String): LiveData<MutableList<ItemsModel>>{
        val listData = MutableLiveData<MutableList<ItemsModel>>()
        val ref = firebaseDatabase.getReference("Items")
        val query: Query = ref.orderByChild("categoryId").equalTo(categoryId)

        query.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemsModel>()
                for(ds in snapshot.children){
                    val items = ds.getValue(ItemsModel::class.java)
                    if(items!=null){
                        list.add(items)
                    }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return listData
    }
}