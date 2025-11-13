package com.example.panaderia20

import android.content.Context
import androidx.compose.ui.input.key.type
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.panaderia20.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.remove

object CartManager {

    private const val PREFS_NAME = "CartPrefs"
    private const val CART_KEY = "CartItems"

    // Usamos un MutableLiveData para que la UI pueda observar cambios en el carrito.
    private val _cartItems = MutableLiveData<MutableMap<String, CartItem>>(mutableMapOf())
    val cartItems: LiveData<MutableMap<String, CartItem>> = _cartItems

    private val gson = Gson()

    // Carga el carrito desde SharedPreferences
    fun loadCart(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonCart = sharedPreferences.getString(CART_KEY, null)

        if (jsonCart != null) {
            // Si hay un carrito guardado, lo convertimos de JSON a nuestro Map
            val type = object : TypeToken<MutableMap<String, CartItem>>() {}.type
            val cart: MutableMap<String, CartItem> = gson.fromJson(jsonCart, type)
            _cartItems.value = cart
        } else {
            // Si no hay nada guardado, nos aseguramos de que el carrito esté vacío
            _cartItems.value = mutableMapOf()
        }
    }

    // Guarda el carrito actual en SharedPreferences
    private fun saveCart(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Convertimos nuestro Map a un String en formato JSON
        val jsonCart = gson.toJson(_cartItems.value)
        sharedPreferences.edit().putString(CART_KEY, jsonCart).apply()
    }

    // Limpia el carrito local y el guardado en SharedPreferences
    fun clearCart(context: Context) {
        _cartItems.value = mutableMapOf()
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(CART_KEY).apply()
    }

    fun addProduct(context: Context, product: Product) {
        val currentItems = _cartItems.value ?: mutableMapOf()
        val cartItem = currentItems[product.id]

        if (cartItem != null) {
            // Si el producto ya existe, incrementa la cantidad
            cartItem.cantidad++
        } else {
            // Si es nuevo, lo añade al mapa
            currentItems[product.id] = CartItem(
                productId = product.id,
                nombre = product.nombre,
                precio = product.precio,
                cantidad = 1,
                imageUrl = product.imageUrl
            )
        }
        _cartItems.value = currentItems
        saveCart(context)
    }

    fun removeProduct(context: Context, productId: String) {
        val currentItems = _cartItems.value ?: return
        currentItems.remove(productId)
        _cartItems.value = currentItems
        saveCart(context)
    }

    fun clearCart() {
        _cartItems.value = mutableMapOf()
    }

    fun increaseQuantity(context: Context, productId: String) {
        val currentItems = _cartItems.value ?: return
        currentItems[productId]?.let { it.cantidad++ }
        _cartItems.value = currentItems
        saveCart(context)
    }

    fun decreaseQuantity(context: Context, productId: String) {
        val currentItems = _cartItems.value ?: return
        val item = currentItems[productId] ?: return

        if (item.cantidad > 1) {
            item.cantidad--
        } else {
            currentItems.remove(productId)
        }
        _cartItems.value = currentItems
        saveCart(context)
    }
}