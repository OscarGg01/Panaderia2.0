package com.example.panaderia20

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import com.google.firebase.firestore.DocumentId

// Modelo para los productos DENTRO de un pedido
// CORRECCIÓN: Propiedades cambiadas a 'var' y se añade un constructor vacío (implícito por los valores por defecto).
data class ProductoPedido(
    var productoId: String = "",
    var nombre: String = "",
    var imageUrl: String = "",
    var precioUnitario: Double = 0.0,
    var cantidad: Int = 0
)

data class Pedido(
    @DocumentId
    var id: String = "",
    var usuarioId: String = "",
    var telefonoPedido: String = "",
    @ServerTimestamp
    var fechaPedido: Date? = null,
    var estado: String = "Pendiente",
    var entrega: String = "",
    var direccionEntrega: String? = null,
    var montoTotal: Double = 0.0,
    var productos: List<ProductoPedido> = emptyList()
)