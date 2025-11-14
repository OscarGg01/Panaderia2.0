package com.example.panaderia20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class OrderDetailProductAdapter(private val products: List<ProductoPedido>) : RecyclerView.Adapter<OrderDetailProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quantityTextView: TextView = itemView.findViewById(R.id.tv_product_quantity)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_product_name)
        private val priceTextView: TextView = itemView.findViewById(R.id.tv_product_unit_price)

        fun bind(product: ProductoPedido) {
            quantityTextView.text = "${product.cantidad} x"
            nameTextView.text = product.nombre
            priceTextView.text = String.format(Locale.getDefault(), "(S/. %.2f c/u)", product.precioUnitario)
        }
    }
}