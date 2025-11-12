package com.example.panaderia20

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        backButton = findViewById(R.id.back_button)

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Vincular vistas
        etName = findViewById(R.id.et_name)
        etPhone = findViewById(R.id.et_phone)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.register_progress_bar)

        // Configurar el listener del botón
        btnRegister.setOnClickListener {
            registerUser()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // --- Validaciones ---
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Correo electrónico no válido"
            etEmail.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            etPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Las contraseñas no coinciden"
            etConfirmPassword.requestFocus()
            return
        }

        // Mostrar ProgressBar y deshabilitar botón
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        // --- Crear usuario en Firebase Authentication ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // El usuario se creó en Authentication. Ahora guardamos sus datos en Firestore.
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid

                    if (userId != null) {
                        // Crear un mapa con los datos del usuario
                        val newUser = hashMapOf(
                            "nombre" to name,
                            "telefono" to phone,
                            "correo" to email,
                            "rol" to "cliente"
                        )

                        // Guardar en la colección "users" con el ID de autenticación
                        firestore.collection("users").document(userId)
                            .set(newUser)
                            .addOnSuccessListener {
                                // Éxito al guardar en Firestore
                                progressBar.visibility = View.GONE
                                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()

                                // Navegar a la pantalla principal (o de login)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                // Error al guardar en Firestore
                                progressBar.visibility = View.GONE
                                btnRegister.isEnabled = true
                                Log.e("RegisterActivity", "Error al guardar en Firestore", e)
                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    // Si el registro falla (ej: el correo ya existe), muestra un mensaje.
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    Log.e("RegisterActivity", "Fallo en createUserWithEmailAndPassword", task.exception)
                    Toast.makeText(baseContext, "Fallo en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}