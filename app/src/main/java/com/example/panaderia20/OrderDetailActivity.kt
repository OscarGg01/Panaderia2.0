package com.example.panaderia20

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var wormDotsIndicator: WormDotsIndicator
    private lateinit var productListAdapter: ProductImageAdapter
    private lateinit var rvDetailProductList: RecyclerView
    private lateinit var orderDetailProductAdapter: OrderDetailProductAdapter
    private lateinit var tvDetailTotal: TextView
    private lateinit var btnCancelOrder: Button

    // 1. Añade una referencia a Firestore
    private val db = FirebaseFirestore.getInstance()
    private var currentOrder: Pedido? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar_order_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Vincular vistas
        viewPager = findViewById(R.id.product_image_viewpager)
        wormDotsIndicator = findViewById(R.id.worm_dots_indicator)
        rvDetailProductList = findViewById(R.id.rv_detail_product_list)
        tvDetailTotal = findViewById(R.id.tv_detail_total)
        btnCancelOrder = findViewById(R.id.btn_cancel_order)

        val orderJson = intent.getStringExtra("ORDER_DETAIL_JSON")
        if (orderJson == null) {
            Toast.makeText(this, "Error al cargar el detalle del pedido.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Guardamos el pedido actual en una variable de la clase
        currentOrder = Gson().fromJson(orderJson, Pedido::class.java)
        currentOrder?.let { displayOrderDetails(it) }

        // 2. Lógica del botón de cancelar
        btnCancelOrder.setOnClickListener {
            handleCancelOrder()
        }
    }

    private fun handleCancelOrder() {
        val order = currentOrder ?: return

        // Si el pedido ya está cancelado, no hacer nada
        if (order.estado == "Cancelado") {
            Toast.makeText(this, "Este pedido ya ha sido cancelado.", Toast.LENGTH_SHORT).show()
            return
        }

        val orderDate = order.fechaPedido ?: return
        val currentDate = Date() // Hora actual

        // Calcula la diferencia en milisegundos
        val diffInMillis = currentDate.time - orderDate.time
        // Convierte la diferencia a minutos
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)

        // 3. Comprueba si han pasado menos de 10 minutos
        if (diffInMinutes < 10) {
            // Si está dentro del tiempo, procede a cancelar
            cancelOrderInFirestore(order)
        } else {
            // Si ha pasado más tiempo, muestra un mensaje
            Toast.makeText(this, "Solo puedes cancelar un pedido dentro de los primeros 10 minutos.", Toast.LENGTH_LONG).show()
        }
    }

    private fun cancelOrderInFirestore(order: Pedido) {
        // Usamos el ID del pedido que ahora tenemos en el modelo
        db.collection("pedidos").document(order.id)
            .update("estado", "Cancelado")
            .addOnSuccessListener {
                Toast.makeText(this, "Pedido cancelado con éxito.", Toast.LENGTH_SHORT).show()
                // 4. Devolvemos un resultado a la actividad anterior para que se actualice
                val resultIntent = Intent()
                resultIntent.putExtra("ORDER_UPDATED", true)
                setResult(Activity.RESULT_OK, resultIntent)
                finish() // Cierra la pantalla de detalle
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cancelar el pedido: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun displayOrderDetails(order: Pedido) {
        // ... (el resto de esta función no cambia)
        productListAdapter = ProductImageAdapter(order.productos)
        viewPager.adapter = productListAdapter
        wormDotsIndicator.attachTo(viewPager)
        rvDetailProductList.isNestedScrollingEnabled = false
        orderDetailProductAdapter = OrderDetailProductAdapter(order.productos)
        rvDetailProductList.adapter = orderDetailProductAdapter
        tvDetailTotal.text = String.format(Locale.getDefault(), "Total: S/. %.2f", order.montoTotal)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}