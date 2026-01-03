package com.example.coffeeapp.Domain

data class CartTotals(
    val subtotal: Double,
    val delivery: Double,
    val tax: Double,
    val total: Double
)
