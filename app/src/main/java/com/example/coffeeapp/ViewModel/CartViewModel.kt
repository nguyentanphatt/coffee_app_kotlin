package com.example.coffeeapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coffeeapp.Domain.ItemsModel
import com.example.coffeeapp.Repository.CartRepository

class CartViewModel : ViewModel() {
    private val repository = CartRepository()

    private val _cartItems = MutableLiveData<MutableList<ItemsModel>>()
    val cartItems: LiveData<MutableList<ItemsModel>> = _cartItems

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice

    private val _itemCount = MutableLiveData<Int>()
    val itemCount: LiveData<Int> = _itemCount

    private val _operationStatus = MutableLiveData<OperationStatus>()
    val operationStatus: LiveData<OperationStatus> = _operationStatus

    fun loadCartItems(userId: String) {
        repository.loadCartItems(userId).observeForever { items ->
            _cartItems.value = items
            calculateTotals(items)
        }
    }

    fun addToCart(userId: String, item: ItemsModel) {
        repository.addToCart(userId, item) { success ->
            if (success) {
                _operationStatus.value = OperationStatus.Success("Item added to cart")
            } else {
                _operationStatus.value = OperationStatus.Error("Failed to add item to cart")
            }
        }
    }

    fun removeFromCart(userId: String, itemTitle: String) {
        repository.removeFromCart(userId, itemTitle) { success ->
            if (success) {
                _operationStatus.value = OperationStatus.Success("Item removed from cart")
            } else {
                _operationStatus.value = OperationStatus.Error("Failed to remove item from cart")
            }
        }
    }

    fun updateItemQuantity(userId: String, itemTitle: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(userId, itemTitle)
            return
        }

        repository.updateCartItemQuantity(userId, itemTitle, quantity) { success ->
            if (success) {
                _operationStatus.value = OperationStatus.Success("Quantity updated")
            } else {
                _operationStatus.value = OperationStatus.Error("Failed to update quantity")
            }
        }
    }

    fun increaseQuantity(userId: String, item: ItemsModel) {
        val newQuantity = item.numberInCart + 1
        updateItemQuantity(userId, item.title, newQuantity)
    }

    fun decreaseQuantity(userId: String, item: ItemsModel) {
        val newQuantity = item.numberInCart - 1
        updateItemQuantity(userId, item.title, newQuantity)
    }

    fun clearCart(userId: String) {
        repository.clearCart(userId) { success ->
            if (success) {
                _operationStatus.value = OperationStatus.Success("Cart cleared")
            } else {
                _operationStatus.value = OperationStatus.Error("Failed to clear cart")
            }
        }
    }

    private fun calculateTotals(items: MutableList<ItemsModel>) {
        var total = 0.0
        var count = 0

        for (item in items) {
            total += item.price * item.numberInCart
            count += item.numberInCart
        }

        _totalPrice.value = total
        _itemCount.value = count
    }

    fun isCartEmpty(): Boolean {
        return _cartItems.value.isNullOrEmpty()
    }

    sealed class OperationStatus {
        data class Success(val message: String) : OperationStatus()
        data class Error(val message: String) : OperationStatus()
    }
}