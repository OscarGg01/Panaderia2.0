package com.example.panaderia20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onDeleteClick: (CartItem) -> Unit,
    private val onIncreaseClick: (CartItem) -> Unit,
    private val onDecreaseClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.cart_product_image)
        val productName: TextView = itemView.findViewById(R.id.cart_product_name)
        val productPrice: TextView = itemView.findViewById(R.id.cart_product_price)
        val productQuantity: TextView = itemView.findViewById(R.id.cart_product_quantity)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_item_button)
        val increaseButton: ImageButton = itemView.findViewById(R.id.increase_quantity_button)
        val decreaseButton: ImageButton = itemView.findViewById(R.id.decrease_quantity_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item_layout, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentCartItem = cartItems[position]

        // Cargar la imagen del producto
        Glide.with(holder.itemView.context)
            .load(currentCartItem.imageUrl)
            .into(holder.productImage)

        // Calcular el precio total para este item (precio unitario * cantidad)
        val totalPrice = currentCartItem.precio * currentCartItem.cantidad

        // Asignar los valores a las vistas
        holder.productName.text = currentCartItem.nombre
        holder.productPrice.text = "S/. ${String.format("%.2f", totalPrice)}" // Muestra el precio total
        holder.productQuantity.text = currentCartItem.cantidad.toString() // Muestra solo el número de la cantidad

        // Asignar las funciones a los botones
        holder.deleteButton.setOnClickListener { onDeleteClick(currentCartItem) }
        holder.increaseButton.setOnClickListener { onIncreaseClick(currentCartItem) }
        holder.decreaseButton.setOnClickListener { onDecreaseClick(currentCartItem) }
    }

    override fun getItemCount() = cartItems.size

    fun updateItems(newItems: MutableList<CartItem>) {
        this.cartItems = newItems
        notifyDataSetChanged()
    }
}