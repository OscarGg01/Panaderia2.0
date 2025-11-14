package com.example.panaderia20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(private val orders: List<Pedido>, private val onOrderClick: (Pedido) -> Unit) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order, onOrderClick)
    }

    override fun getItemCount(): Int = orders.size

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val statusTextView: TextView = itemView.findViewById(R.id.tv_order_status)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_order_date)
        private val productsTextView: TextView = itemView.findViewById(R.id.tv_order_products)
        private val deliveryTypeTextView: TextView = itemView.findViewById(R.id.tv_order_delivery_type)
        private val totalTextView: TextView = itemView.findViewById(R.id.tv_order_total)

        fun bind(order: Pedido, onOrderClick: (Pedido) -> Unit) {
            statusTextView.text = order.estado
            totalTextView.text = String.format(Locale.getDefault(), "S/. %.2f", order.montoTotal)
            deliveryTypeTextView.text = "Entrega: ${order.entrega}"

            if (order.estado == "Cancelado") {
                statusTextView.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
            } else {
                statusTextView.setTextColor(itemView.context.getColor(android.R.color.black))
            }

            // Formatear la fecha
            order.fechaPedido?.let {
                val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("es", "ES"))
                formatter.timeZone = TimeZone.getTimeZone("GMT-5")
                dateTextView.text = formatter.format(it)
            }

            // Construir la lista de productos como un solo string
            val productsString = order.productos.joinToString("\n") {
                "• ${it.cantidad} x ${it.nombre}"
            }

            // Mueve la línea aquí para que esté con las demás asignaciones
            productsTextView.text = productsString

            // El listener siempre debe ir al final
            itemView.setOnClickListener {
                onOrderClick(order)
            }
        }
    }
}