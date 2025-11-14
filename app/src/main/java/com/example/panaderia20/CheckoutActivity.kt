package com.example.panaderia20

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutActivity : AppCompatActivity(), OnMapReadyCallback {

    // --- Vistas y variables ---
    private lateinit var recyclerView: RecyclerView
    private lateinit var checkoutAdapter: CheckoutAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var deliveryRadioGroup: RadioGroup
    private lateinit var mapSectionContainer: LinearLayout
    private lateinit var orderButtonPickup: Button
    private lateinit var directionsButton: Button
    private lateinit var deliverySectionContainer: LinearLayout
    private lateinit var addressEditText: EditText
    private lateinit var openMapButton: ImageButton
    private lateinit var phoneEditText: EditText
    private lateinit var orderButtonDelivery: Button
    private lateinit var autofillPhoneButton: TextView
    private var subtotal: Double = 0.0
    private val deliveryFee: Double = 5.0
    private var googleMap: GoogleMap? = null
    private val panaderiaLocation = LatLng(-12.068231593872142, -75.2104745717286)
    private var userRegisteredPhone: String? = null

    // --- Firebase ---
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // --- Lanzadores ---
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) showDirections() else Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_LONG).show()
    }
    private val mapPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val address = result.data?.getStringExtra("selected_address")
            addressEditText.setText(address)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        bindViews()
        setupRecyclerView()
        setupListeners()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val cartItems = CartManager.cartItems.value?.values?.toList() ?: emptyList()
        subtotal = cartItems.sumOf { it.precio * it.cantidad }
        updateTotalPrice()
        updateUIVisibility()
        loadUserPhoneNumber()
        autofillPhoneButton.isEnabled = false
    }

    // ... (Las funciones bindViews, setupListeners, etc., hasta processOrder no cambian) ...
    private fun bindViews() {
        recyclerView = findViewById(R.id.checkout_recycler_view)
        totalPriceTextView = findViewById(R.id.checkout_total_price)
        deliveryRadioGroup = findViewById(R.id.delivery_radio_group)
        mapSectionContainer = findViewById(R.id.map_section_container)
        orderButtonPickup = findViewById(R.id.order_button)
        directionsButton = findViewById(R.id.directions_button)
        deliverySectionContainer = findViewById(R.id.delivery_section_container)
        addressEditText = findViewById(R.id.address_edit_text)
        openMapButton = findViewById(R.id.open_map_button)
        phoneEditText = findViewById(R.id.phone_edit_text)
        orderButtonDelivery = findViewById(R.id.order_button_delivery)
        autofillPhoneButton = findViewById(R.id.autofill_phone_button)
    }

    private fun setupListeners() {
        deliveryRadioGroup.setOnCheckedChangeListener { _, _ ->
            updateTotalPrice()
            updateUIVisibility()
        }
        directionsButton.setOnClickListener { checkLocationPermissionAndShowDirections() }
        openMapButton.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            mapPickerLauncher.launch(intent)
        }
        orderButtonPickup.setOnClickListener { showConfirmationDialog() }
        orderButtonDelivery.setOnClickListener { showConfirmationDialog() }
        autofillPhoneButton.setOnClickListener {
            userRegisteredPhone?.let { phone ->
                phoneEditText.setText(phone)
            }
        }
    }

    private fun showConfirmationDialog() {
        val isDelivery = deliveryRadioGroup.checkedRadioButtonId == R.id.radio_delivery
        if (isDelivery) {
            val address = addressEditText.text.toString()
            val phone = phoneEditText.text.toString()
            if (address.isBlank() || phone.isBlank()) {
                Toast.makeText(this, "Por favor, complete la dirección y el número de teléfono.", Toast.LENGTH_LONG).show()
                return
            }
            if (phone.length != 9) {
                Toast.makeText(this, "Por favor, ingrese un número de teléfono válido de 9 dígitos.", Toast.LENGTH_LONG).show()
                return
            }
        }
        AlertDialog.Builder(this)
            .setTitle("Confirmar Pedido")
            .setMessage("¿Estás seguro de que deseas realizar este pedido?")
            .setPositiveButton("Confirmar") { _, _ -> processOrder() }
            .setNegativeButton("Cancelar", null)
            .setIcon(R.drawable.ic_not)
            .show()
    }

    // --- FUNCIÓN `processOrder` COMPLETAMENTE ACTUALIZADA ---
    private fun processOrder() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error de autenticación. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show()
            return
        }

        // 1. Recopilar todos los datos para el objeto Pedido
        val isDelivery = deliveryRadioGroup.checkedRadioButtonId == R.id.radio_delivery
        val tipoEntrega = if (isDelivery) "Delivery" else "Recojo en tienda"
        val montoFinal = if (isDelivery) subtotal + deliveryFee else subtotal

        // --- LÓGICA ACTUALIZADA AQUÍ ---
        val telefonoPedido: String
        val direccion: String? // La dirección ahora puede ser nula

        if (isDelivery) {
            telefonoPedido = phoneEditText.text.toString()
            direccion = addressEditText.text.toString()
        } else {
            telefonoPedido = "" // Sin teléfono si es recojo
            direccion = null   // Sin dirección si es recojo
        }

        val productosDelPedido = CartManager.cartItems.value?.values?.map { cartItem ->
            ProductoPedido(
                productoId = cartItem.productId,
                nombre = cartItem.nombre,
                imageUrl = cartItem.imageUrl,
                precioUnitario = cartItem.precio,
                cantidad = cartItem.cantidad
            )
        } ?: emptyList()

        // 2. Crear el objeto Pedido con el nuevo campo
        val nuevoPedido = Pedido(
            usuarioId = userId,
            telefonoPedido = telefonoPedido,
            entrega = tipoEntrega,
            direccionEntrega = direccion, // Asignamos la dirección (que será null si no es delivery)
            montoTotal = montoFinal,
            productos = productosDelPedido
        )

        // 3. Guardar el objeto en la colección "pedidos" de Firestore
        db.collection("pedidos")
            .add(nuevoPedido)
            .addOnSuccessListener {
                Log.d("CheckoutActivity", "Pedido guardado con ID: ${it.id}")
                CartManager.clearCart(this)
                val intent = Intent(this, OrderSuccessActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("CheckoutActivity", "Error al guardar el pedido", e)
                Toast.makeText(this, "Error al procesar el pedido. Intente de nuevo.", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadUserPhoneNumber() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            autofillPhoneButton.isEnabled = false
            return
        }
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val phone = document.getString("telefono")
                    if (phone != null) {
                        userRegisteredPhone = phone
                        autofillPhoneButton.isEnabled = true
                    } else {
                        autofillPhoneButton.isEnabled = false
                    }
                } else {
                    autofillPhoneButton.isEnabled = false
                }
            }
            .addOnFailureListener {
                autofillPhoneButton.isEnabled = false
            }
    }

    // ... (El resto de las funciones no cambian) ...
    private fun updateUIVisibility() {
        val isPickupSelected = deliveryRadioGroup.checkedRadioButtonId == R.id.radio_pickup
        mapSectionContainer.visibility = if (isPickupSelected) View.VISIBLE else View.GONE
        deliverySectionContainer.visibility = if (isPickupSelected) View.GONE else View.VISIBLE
    }
    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_checkout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
    private fun setupRecyclerView() {
        val cartItems = CartManager.cartItems.value?.values?.toList() ?: emptyList()
        checkoutAdapter = CheckoutAdapter(cartItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = checkoutAdapter
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.addMarker(MarkerOptions().position(panaderiaLocation).title("Panadería Delicia"))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(panaderiaLocation, 15f))
        googleMap?.uiSettings?.isZoomControlsEnabled = true
    }
    private fun checkLocationPermissionAndShowDirections() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            showDirections()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private fun showDirections() {
        try {
            val destination = "${panaderiaLocation.latitude},${panaderiaLocation.longitude}"
            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destination&travelmode=driving")
            val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir Google Maps.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateTotalPrice() {
        val isDelivery = deliveryRadioGroup.checkedRadioButtonId == R.id.radio_delivery
        val finalTotal = if (isDelivery) subtotal + deliveryFee else subtotal
        totalPriceTextView.text = String.format("Total: S/. %.2f", finalTotal)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}