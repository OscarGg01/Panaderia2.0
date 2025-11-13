package com.example.panaderia20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView

class CheckoutAdapter(private val items: List<CartItem>) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.summary_product_name)
        val quantity: TextView = view.findViewById(R.id.summary_product_quantity)
        val totalPrice: TextView = view.findViewById(R.id.summary_product_total_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.nombre
        holder.quantity.text = "x${item.cantidad}"
        val itemTotalPrice = item.precio * item.cantidad
        holder.totalPrice.text = String.format("S/. %.2f", itemTotalPrice)
    }

    override fun getItemCount() = items.size
}