package com.example.panaderia20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

class ProductAdapter(private val products: List<Product>, private val onProductClick: (Product) -> Unit) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Define el ViewHolder que contiene las vistas de cada item.
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.product_name)
        val description: TextView = itemView.findViewById(R.id.product_description)
        val price: TextView = itemView.findViewById(R.id.product_price)
        val image: ImageView = itemView.findViewById(R.id.product_image)
        val stockStatus: TextView = itemView.findViewById(R.id.stock_status)
        val type: TextView = itemView.findViewById(R.id.product_type)
        val addToCartButton: ImageView = itemView.findViewById(R.id.add_to_cart_button)
        val productCard: ConstraintLayout = itemView.findViewById(R.id.product_card_container)
    }

    // Crea nuevos ViewHolders (llamado por el layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    // Reemplaza el contenido de una vista (llamado por el layout manager)
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Enlaza los datos de texto
        holder.name.text = product.nombre
        holder.description.text = product.descripcion
        holder.type.text = product.tipo
        holder.price.text = String.format(Locale.US,"S/ %.2f", product.precio)

        // Muestra el estado "AGOTADO" si el stock es 0
        holder.stockStatus.visibility = if (product.stock == 0) View.VISIBLE else View.GONE

        // Carga la imagen usando Glide
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            // Las siguientes líneas se comentan temporalmente.
            // Descomenta cuando definas los recursos ic_placeholder_pan y ic_error_image en res/drawable
            // .placeholder(R.drawable.ic_placeholder_pan)
            // .error(R.drawable.ic_error_image)
            .into(holder.image)

        // Opcional: Establecer un listener de click para navegar al detalle del producto
        holder.itemView.setOnClickListener {
            // Aquí puedes implementar la navegación a la pantalla de detalle.
            // Por ejemplo: Toast.makeText(it.context, "Detalle de ${product.nombre}", Toast.LENGTH_SHORT).show()
        }

        // 1. Clic en el botón del carrito (dentro de la tarjeta)
        holder.addToCartButton.setOnClickListener {
            // Por ahora, solo mostramos un mensaje
            Toast.makeText(holder.itemView.context, "${product.nombre} añadido (funcionalidad pendiente)", Toast.LENGTH_SHORT).show()
        }

        // 2. Clic en la tarjeta completa
        holder.productCard.setOnClickListener {
            onProductClick(product) // Llamamos a la lambda que pasamos al constructor
        }
    }

    // Devuelve el tamaño de tu dataset (llamado por el layout manager)
    override fun getItemCount() = products.size
}