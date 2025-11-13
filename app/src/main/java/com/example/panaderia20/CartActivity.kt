package com.example.panaderia20

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartActivity : AppCompatActivity() {

    // Propiedades de la UI
    private lateinit var backButton: ImageButton
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var emptyCartMessage: TextView
    private lateinit var cartAdapter: CartAdapter

    // Vistas para la sección de resumen
    private lateinit var summarySection: LinearLayout
    private lateinit var totalPriceText: TextView
    private lateinit var addMoreProductsButton: Button // CORREGIDO: Tipo de botón correcto
    private lateinit var continuePurchaseButton: Button // CORREGIDO: Tipo de botón correcto

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Vincular todas las vistas del layout
        backButton = findViewById(R.id.back_button_cart)
        cartRecyclerView = findViewById(R.id.cart_recycler_view)
        emptyCartMessage = findViewById(R.id.empty_cart_message)
        summarySection = findViewById(R.id.summary_section)
        totalPriceText = findViewById(R.id.total_price_text)
        addMoreProductsButton = findViewById(R.id.add_more_products_button)
        continuePurchaseButton = findViewById(R.id.continue_purchase_button)

        // Configurar los clics de los botones
        setupButtons()

        // Configurar el RecyclerView
        setupRecyclerView()

        // Empezar a "escuchar" los cambios en el carrito para actualizar la UI
        observeCart()
    }

    private fun setupButtons() {
        backButton.setOnClickListener {
            finish() // Cierra esta actividad y vuelve a la anterior
        }

        addMoreProductsButton.setOnClickListener {
            // Vuelve a la pantalla principal (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            // Estas flags evitan crear múltiples instancias de MainActivity
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Cierra la actividad del carrito
        }

        continuePurchaseButton.setOnClickListener {
            // Funcionalidad futura
            Toast.makeText(this, "Función de pago no implementada aún", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            mutableListOf(),
            onDeleteClick = { cartItem ->
                CartManager.removeProduct(this, cartItem.productId)
                Toast.makeText(this, "${cartItem.nombre} eliminado", Toast.LENGTH_SHORT).show()
            },
            onIncreaseClick = { cartItem -> CartManager.increaseQuantity(this, cartItem.productId) },
            onDecreaseClick = { cartItem -> CartManager.decreaseQuantity(this, cartItem.productId) }
        )
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter
    }

    private fun observeCart() {
        CartManager.cartItems.observe(this) { itemsMap ->
            val itemsList = itemsMap.values.toMutableList()
            var totalPrice = 0.0

            if (itemsList.isEmpty()) {
                // Si el carrito está vacío, oculta la lista y la sección de total
                emptyCartMessage.visibility = View.VISIBLE
                cartRecyclerView.visibility = View.GONE
                summarySection.visibility = View.GONE
            } else {
                // Si hay productos, muestra la lista y la sección de total
                emptyCartMessage.visibility = View.GONE
                cartRecyclerView.visibility = View.VISIBLE
                summarySection.visibility = View.VISIBLE

                // Calcula el precio total sumando el (precio * cantidad) de cada item
                for (item in itemsList) {
                    totalPrice += item.precio * item.cantidad
                }
            }

            // Actualiza la lista en el adaptador
            cartAdapter.updateItems(itemsList)
            // Actualiza el texto del precio total
            totalPriceText.text = "S/. ${String.format("%.2f", totalPrice)}"
        }
    }
}