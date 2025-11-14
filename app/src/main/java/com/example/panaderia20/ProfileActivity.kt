package com.example.panaderia20

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var btnLogout: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var btnMyOrders: Button
    private lateinit var btnBackToCatalog: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Vincular vistas
        tvName = findViewById(R.id.tv_profile_name)
        tvEmail = findViewById(R.id.tv_profile_email)
        tvPhone = findViewById(R.id.tv_profile_phone)
        btnLogout = findViewById(R.id.btn_logout)
        progressBar = findViewById(R.id.profile_progress_bar)
        btnMyOrders = findViewById(R.id.btn_my_orders)
        btnBackToCatalog = findViewById(R.id.btn_back_to_catalog)

        // Cargar datos del usuario
        loadUserProfile()

        // Listener para el botón de Cerrar Sesión
        btnLogout.setOnClickListener {
            logoutUser()
        }

        // Listener para el botón de volver al catálogo
        btnBackToCatalog.setOnClickListener {
            finish() // Simplemente cierra esta actividad para volver a la anterior
        }

        btnMyOrders.setOnClickListener {
            // Abre la nueva actividad que muestra la lista de pedidos
            val intent = Intent(this, MyOrdersActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfile() {
        progressBar.visibility = View.VISIBLE
        val user = auth.currentUser

        if (user == null) {
            // Si por alguna razón no hay usuario, volver a la pantalla principal
            goToMainActivity()
            return
        }

        // El correo lo obtenemos directamente de Firebase Auth
        tvEmail.text = user.email

        // Para el nombre y teléfono, consultamos Firestore
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                if (document != null && document.exists()) {
                    // Usamos la clase User que ya creamos
                    val userData = document.toObject(User::class.java)
                    tvName.text = userData?.nombre ?: "Nombre no disponible"
                    tvPhone.text = userData?.telefono ?: "Teléfono no disponible"
                } else {
                    Toast.makeText(this, "No se encontraron datos del perfil.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error al cargar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logoutUser() {
        // Limpiar el carrito local antes de cerrar sesión
        CartManager.clearCart(this)
        auth.signOut()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        goToMainActivity()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // Estas flags limpian el historial de actividades para que el usuario no pueda volver atrás
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}