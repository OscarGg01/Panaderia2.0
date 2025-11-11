package com.example.panaderia20

import com.google.firebase.firestore.DocumentId

// Usamos @JvmField para asegurar que los nombres de las propiedades coincidan exactamente con las claves de Firestore.
data class Product(
    // DocumentId mapea automáticamente el ID del documento de Firestore a esta propiedad.
    @DocumentId
    val id: String = "",
    val descripcion: String = "",
    val nombre: String = "",
    val imageUrl: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val tipo: String = ""
)