package com.example.panaderia20

data class CartItem(
    val productId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    var cantidad: Int = 0,
    val imageUrl: String = ""
)