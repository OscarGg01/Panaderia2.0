// En: OrderSuccessActivity.kt
package com.example.panaderia20

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OrderSuccessActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_success)

        val backToHomeButton: Button = findViewById(R.id.back_to_home_button)

        backToHomeButton.setOnClickListener {
            // Creamos un intent para ir a MainActivity
            val intent = Intent(this, MainActivity::class.java)
            // Estas flags limpian todas las actividades anteriores (Checkout, Cart)
            // y se aseguran de que MainActivity sea la única en la pila.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
    }
}