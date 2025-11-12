package com.example.panaderia20

import com.google.firebase.firestore.PropertyName

// Usamos @PropertyName cuando el nombre del campo en Kotlin
// es diferente al de Firestore, pero es buena práctica usarlo
// para evitar problemas de ofuscación de código con ProGuard.
data class User(
    @get:PropertyName("nombre") @set:PropertyName("nombre")
    var nombre: String = "",

    @get:PropertyName("telefono") @set:PropertyName("telefono")
    var telefono: String = "",

    @get:PropertyName("correo") @set:PropertyName("correo")
    var correo: String = ""
) {
    // Constructor vacío requerido por Firestore para deserializar los datos.
    constructor() : this("", "", "")
}