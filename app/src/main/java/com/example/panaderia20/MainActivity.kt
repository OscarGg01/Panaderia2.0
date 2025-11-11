package com.example.panaderia20

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager // Importación clave para el grid
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.panaderia20.Product
import com.example.panaderia20.ProductAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.products_recycler_view)

        // Configuración para 2 productos por fila y desplazamiento (scroll) vertical automático
        // GridLayoutManager gestiona automáticamente el desplazamiento necesario.
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter

        loadProducts()
    }

    private fun loadProducts() {
        // Usamos "product" (singular) para coincidir con el nombre de la colección en Firestore.
        db.collection("product")
            .orderBy("nombre", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    try {
                        val product = Product(
                            id = document.id,
                            nombre = document.getString("nombre") ?: "Sin Nombre",
                            descripcion = document.getString("descripcion") ?: "Sin descripción",
                            precio = document.getDouble("precio") ?: 0.0,
                            tipo = document.getString("tipo") ?: "Sin tipo",
                            stock = (document.getLong("stock") ?: 0).toInt(),
                            imageUrl = document.getString("imageUrl") ?: ""
                        )
                        productList.add(product)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error al parsear documento: ${document.id}", e)
                    }
                }
                productAdapter.notifyDataSetChanged()
                Log.d("MainActivity", "Productos cargados exitosamente: ${productList.size}")
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error al obtener documentos: ", exception)
            }
    }
}