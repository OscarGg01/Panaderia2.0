package com.example.panaderia20

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.geometry.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noOrdersTextView: TextView
    private lateinit var orderAdapter: OrderAdapter

    // 1. Crea un lanzador de actividad que escucha un resultado
    private val orderDetailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Comprueba si la actividad de detalle devolvió un resultado "OK"
        if (result.resultCode == Activity.RESULT_OK) {
            // Si es así, significa que un pedido fue modificado (cancelado).
            // Volvemos a cargar toda la lista para reflejar el cambio.
            loadOrders()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        // Configurar Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar_my_orders)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Vincular vistas
        ordersRecyclerView = findViewById(R.id.orders_recycler_view)
        progressBar = findViewById(R.id.orders_progress_bar)
        noOrdersTextView = findViewById(R.id.tv_no_orders)

        // Configurar RecyclerView
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar los pedidos
        loadOrders()
    }

    private fun loadOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            noOrdersTextView.visibility = View.VISIBLE
            return
        }

        progressBar.visibility = View.VISIBLE
        noOrdersTextView.visibility = View.GONE // Ocultar mensaje mientras se carga

        db.collection("pedidos")
            .whereEqualTo("usuarioId", userId)
            .orderBy("fechaPedido", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                if (documents.isEmpty) {
                    noOrdersTextView.visibility = View.VISIBLE
                } else {
                    val orders = documents.toObjects(Pedido::class.java)
                    orderAdapter = OrderAdapter(orders) { selectedOrder ->
                        val intent = Intent(this, OrderDetailActivity::class.java)
                        val orderJson = Gson().toJson(selectedOrder)
                        intent.putExtra("ORDER_DETAIL_JSON", orderJson)
                        // 2. Inicia la actividad usando el lanzador que espera un resultado
                        orderDetailLauncher.launch(intent)
                    }
                    ordersRecyclerView.adapter = orderAdapter
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                noOrdersTextView.text = "Error al cargar los pedidos."
                noOrdersTextView.visibility = View.VISIBLE
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}