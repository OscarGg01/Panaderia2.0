package com.example.panaderia20

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.semantics.text
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
            // Si no hay usuario, no hay nada que mostrar.
            noOrdersTextView.visibility = View.VISIBLE
            return
        }

        progressBar.visibility = View.VISIBLE

        db.collection("pedidos")
            .whereEqualTo("usuarioId", userId) // <-- LA CLAVE: Filtra solo los pedidos de este usuario
            .orderBy("fechaPedido", Query.Direction.DESCENDING) // Muestra los más recientes primero
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                if (documents.isEmpty) {
                    noOrdersTextView.visibility = View.VISIBLE
                } else {
                    val orders = documents.toObjects(Pedido::class.java)
                    // Pasa la lógica del clic aquí
                    orderAdapter = OrderAdapter(orders) { selectedOrder ->
                        val intent = Intent(this, OrderDetailActivity::class.java)
                        val orderJson = Gson().toJson(selectedOrder)
                        intent.putExtra("ORDER_DETAIL_JSON", orderJson)
                        startActivity(intent)
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

    // Para que el botón de "atrás" en la toolbar funcione
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}