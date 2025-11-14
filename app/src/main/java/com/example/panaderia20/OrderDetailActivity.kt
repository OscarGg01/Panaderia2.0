package com.example.panaderia20

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.google.gson.Gson
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var wormDotsIndicator: WormDotsIndicator
    private lateinit var productListAdapter: ProductImageAdapter

    // 1. Declara el RecyclerView y su adaptador
    private lateinit var rvDetailProductList: RecyclerView
    private lateinit var orderDetailProductAdapter: OrderDetailProductAdapter

    private lateinit var tvDetailTotal: TextView
    private lateinit var btnCancelOrder: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar_order_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Vincular vistas
        viewPager = findViewById(R.id.product_image_viewpager)
        wormDotsIndicator = findViewById(R.id.worm_dots_indicator)

        // 2. Vincula el RecyclerView
        rvDetailProductList = findViewById(R.id.rv_detail_product_list)

        tvDetailTotal = findViewById(R.id.tv_detail_total)
        btnCancelOrder = findViewById(R.id.btn_cancel_order)

        val orderJson = intent.getStringExtra("ORDER_DETAIL_JSON")
        if (orderJson == null) {
            Toast.makeText(this, "Error al cargar el detalle del pedido.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val order = Gson().fromJson(orderJson, Pedido::class.java)
        displayOrderDetails(order)

        btnCancelOrder.setOnClickListener {
            Toast.makeText(this, "Función 'Cancelar Pedido' no implementada.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayOrderDetails(order: Pedido) {
        // Configurar el ViewPager2 con las imágenes
        productListAdapter = ProductImageAdapter(order.productos)
        viewPager.adapter = productListAdapter
        wormDotsIndicator.attachTo(viewPager)

        // 3. Configura el RecyclerView de la lista de productos
        rvDetailProductList.isNestedScrollingEnabled = false // Mejora el scroll dentro del ScrollView
        orderDetailProductAdapter = OrderDetailProductAdapter(order.productos)
        rvDetailProductList.adapter = orderDetailProductAdapter

        // Mostrar el monto total
        tvDetailTotal.text = String.format(Locale.getDefault(), "Total: S/. %.2f", order.montoTotal)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}