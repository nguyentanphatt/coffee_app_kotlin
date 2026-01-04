package com.example.coffeeapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.Repository.FavoriteRepository

class FavoriteViewModel: ViewModel() {
    private val repository = FavoriteRepository()
    private val _favoriteItem = MutableLiveData<MutableList<ItemsModel>>()
    val favoriteItem: LiveData<MutableList<ItemsModel>> = _favoriteItem

    private val _operationStatus = MutableLiveData<OperationStatus>()
    val operationStatus: LiveData<OperationStatus> = _operationStatus

    fun loadFavoriteItem(userId: String) {
        repository.loadFavoriteItem(userId).observeForever { items ->
            _favoriteItem.value = items
        }
    }

    fun addToFavorite(userId: String, item: ItemsModel) {
        repository.addToFavorite(userId, item) { success ->
            if (success) {
                _operationStatus.value = OperationStatus.Success("Item added to favorites", true)
            } else {
                _operationStatus.value = OperationStatus.Error("Failed to add item to favorites")
            }
        }
    }

    fun removeFromFavorite(userId: String, itemTitle: String) {
        repository.removeFromFavorite(userId, itemTitle) { success ->
            if (success) {
                _operationStatus.value = OperationStatus.Success("Item removed from favorites", false)
            } else {
                _operationStatus.value =
                    OperationStatus.Error("Failed to remove item from favorites")
            }
        }
    }

    fun isFavorite(userId: String, itemTitle: String, onResult: (Boolean) -> Unit) {
        repository.isFavorite(userId, itemTitle) { isFavorite ->
            onResult(isFavorite)
        }
    }

    fun toggleFavorite(userId: String, item: ItemsModel) {
        repository.toggleFavorite(userId, item) { success, isAdded ->
            if (success) {
                val message = if (isAdded) "Added to favorites" else "Removed from favorites"
                _operationStatus.value = OperationStatus.Success(message, isAdded)
            } else {
                _operationStatus.value = OperationStatus.Error("Failed to update favorites")
            }
        }
    }

    fun checkIsFavorite(userId: String, itemTitle: String) {
        repository.isFavorite(userId, itemTitle) { isFav ->
            _operationStatus.value = OperationStatus.CheckOnly(isFav)
        }
    }


    sealed class OperationStatus {
        data class Success(val message: String, val isFavorite: Boolean) : OperationStatus()
        data class Error(val message: String) : OperationStatus()
        data class CheckOnly(val isFavorite: Boolean) : OperationStatus()
    }
}