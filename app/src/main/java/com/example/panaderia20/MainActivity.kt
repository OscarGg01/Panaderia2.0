package com.example.panaderia20

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.with
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var searchEditText: EditText
    private lateinit var progressBar: ProgressBar

    // Listas para la gestión de datos
    private val fullProductList = mutableListOf<Product>()
    private val productList = mutableListOf<Product>() // Lista que usa el adaptador

    // Variables para gestionar los filtros
    private var currentSearchQuery: String = ""
    private var activeCategoryFilter: String? = null

    // Mapa para gestionar los botones de filtro
    private lateinit var filterButtons: Map<String, View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        recyclerView = findViewById(R.id.products_recycler_view)
        searchEditText = findViewById(R.id.search_edittext)
        progressBar = findViewById(R.id.progress_bar)

        // Configurar RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductAdapter(productList) { product -> showProductDetail(product)
        }
        recyclerView.adapter = productAdapter

        // Configurar listeners
        setupFilterButtons()
        setupSearchListener()

        // Cargar productos
        loadProducts()
    }

    private fun loadProducts() {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("product")
            .get()
            .addOnSuccessListener { result ->
                fullProductList.clear()
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
                        fullProductList.add(product)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error al parsear documento: ${document.id}", e)
                    }
                }
                applyFilters() // Aplicar filtros iniciales (ninguno)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error cargando productos.", exception)
                progressBar.visibility = View.GONE
            }
    }

    private fun setupFilterButtons() {
        filterButtons = mapOf(
            "Saludable" to findViewById(R.id.filter_healthy),
            "Dulce" to findViewById(R.id.filter_sweet),
            "Artesanal" to findViewById(R.id.filter_artisan),
            "Bebida" to findViewById(R.id.filter_drink)
        )

        filterButtons.forEach { (category, view) ->
            view.setOnClickListener {
                // Si el filtro presionado ya estaba activo, se desactiva.
                if (activeCategoryFilter == category) {
                    activeCategoryFilter = null
                } else {
                    // Si no, se activa el nuevo filtro.
                    activeCategoryFilter = category
                }
                updateFilterButtonsUI()
                applyFilters()
            }
        }
    }

    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateFilterButtonsUI() {
        filterButtons.forEach { (category, view) ->
            view.isSelected = (activeCategoryFilter == category)
        }
    }

    private fun applyFilters() {
        // 1. Empezar con la lista completa
        var filteredList = fullProductList

        // 2. Aplicar filtro de categoría si existe
        activeCategoryFilter?.let { category ->
            filteredList = filteredList.filter { product ->
                product.tipo.equals(category, ignoreCase = true)
            }.toMutableList()
        }

        // 3. Aplicar filtro de búsqueda si existe
        if (currentSearchQuery.isNotEmpty()) {
            val lowerCaseQuery = currentSearchQuery.toLowerCase(Locale.getDefault())
            filteredList = filteredList.filter { product ->
                product.nombre.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        product.tipo.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)
            }.toMutableList()
        }

        // 4. Actualizar la lista del adaptador y notificar
        productList.clear()
        productList.addAll(filteredList)
        productAdapter.notifyDataSetChanged()
    }

    private fun showProductDetail(product: Product) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_product_detail, null)
        dialog.setContentView(view)

        val detailImage = view.findViewById<ImageView>(R.id.detail_product_image)
        val detailName = view.findViewById<TextView>(R.id.detail_product_name)
        val detailDescription = view.findViewById<TextView>(R.id.detail_product_description)
        val detailPrice = view.findViewById<TextView>(R.id.detail_product_price)
        val detailAddToCartButton = view.findViewById<android.widget.Button>(R.id.detail_add_to_cart_button)
        detailName.text = product.nombre
        detailDescription.text = product.descripcion
        detailPrice.text = String.format("$ %.2f", product.precio)

        Glide.with(this)
            .load(product.imageUrl)
            .into(detailImage)

        detailAddToCartButton.setOnClickListener {
            // Lógica futura del carrito
            dialog.dismiss()
        }

        dialog.show()
    }
}