package com.example.panaderia20

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var searchEditText: EditText
    private lateinit var progressBar: ProgressBar

    private lateinit var cartButton: ImageButton
    private lateinit var cartBadge: TextView // <-- NUEVO: Referencia al contador

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var goToLoginButton: TextView
    private lateinit var goToProfileButton: ImageButton

    // Listas para la gestión de datos
    private val fullProductList = mutableListOf<Product>()
    private val productList = mutableListOf<Product>() // Lista que usa el adaptador

    // Variables para gestionar los filtros
    private var currentSearchQuery: String = ""
    private var activeCategoryFilter: String? = null

    // Mapa para gestionar los botones de filtro
    private lateinit var filterButtons: Map<String, View>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- Inicialización de Firebase ---
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            CartManager.loadCart(this)
        }
        firestore = FirebaseFirestore.getInstance()

        // --- Vinculación de Vistas ---
        recyclerView = findViewById(R.id.products_recycler_view)
        searchEditText = findViewById(R.id.search_edittext)
        progressBar = findViewById(R.id.progress_bar)
        goToLoginButton = findViewById(R.id.go_to_login_button)
        goToProfileButton = findViewById(R.id.go_to_profile_button)
        cartButton = findViewById(R.id.cart_button)
        cartBadge = findViewById(R.id.cart_badge) // <-- NUEVO: Vinculamos el contador

        // --- Configuración del RecyclerView ---
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductAdapter(this, productList) { product ->
            showProductDetail(product)
        }
        recyclerView.adapter = productAdapter

        // --- Configuración de Listeners ---
        setupFilterButtons()
        setupSearchListener()
        setupClickListeners() // <-- NUEVO: Agrupamos los listeners de botones

        // --- Carga de Datos Inicial ---
        loadProducts()
        observeCart() // <-- NUEVO: Empezamos a observar el carrito
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        // Actualiza el contador por si el carrito cambió en otra pantalla
        updateCartBadge(CartManager.cartItems.value?.size ?: 0)
    }

    // NUEVO: Observa los cambios en el carrito y actualiza el contador
    private fun observeCart() {
        CartManager.cartItems.observe(this) { itemsMap ->
            val uniqueProductCount = itemsMap.size
            updateCartBadge(uniqueProductCount)
        }
    }

    // NUEVO: Actualiza la visibilidad y el texto del contador
    private fun updateCartBadge(count: Int) {
        if (count == 0) {
            cartBadge.visibility = View.GONE // Oculta el contador si el carrito está vacío
        } else {
            cartBadge.visibility = View.VISIBLE // Muéstralo si hay productos
            cartBadge.text = count.toString()
        }
    }

    // NUEVO: Agrupamos los listeners para mayor orden
    private fun setupClickListeners() {
        goToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        goToProfileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        cartButton.setOnClickListener {
            handleCartClick()
        }
    }

    private fun handleCartClick() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, CartActivity::class.java))
        } else {
            Toast.makeText(this, "Debes iniciar sesión para ver tu carrito", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun updateUI() {
        if (auth.currentUser != null) {
            goToProfileButton.visibility = View.VISIBLE
            goToLoginButton.visibility = View.GONE
        } else {
            goToProfileButton.visibility = View.GONE
            goToLoginButton.visibility = View.VISIBLE
        }
    }

    private fun loadProducts() {
        progressBar.visibility = View.VISIBLE
        firestore.collection("product")
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
                applyFilters()
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
                activeCategoryFilter = if (activeCategoryFilter == category) null else category
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
        var filteredList = fullProductList.asSequence()

        activeCategoryFilter?.let { category ->
            filteredList = filteredList.filter { it.tipo.equals(category, ignoreCase = true) }
        }

        if (currentSearchQuery.isNotEmpty()) {
            val lowerCaseQuery = currentSearchQuery.toLowerCase(Locale.getDefault())
            filteredList = filteredList.filter {
                it.nombre.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.tipo.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)
            }
        }

        productList.clear()
        productList.addAll(filteredList.toList())
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
        val detailAddToCartButton = view.findViewById<Button>(R.id.detail_add_to_cart_button)

        detailName.text = product.nombre
        detailDescription.text = product.descripcion
        detailPrice.text = String.format("S/. %.2f", product.precio)

        Glide.with(this).load(product.imageUrl).into(detailImage)

        detailAddToCartButton.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this, "Debes iniciar sesión para añadir productos", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                CartManager.addProduct(this, product)
                Toast.makeText(this, "${product.nombre} añadido al carrito", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}