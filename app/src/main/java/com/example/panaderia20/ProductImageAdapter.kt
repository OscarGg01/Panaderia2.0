package com.example.panaderia20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductImageAdapter(private val products: List<ProductoPedido>) : RecyclerView.Adapter<ProductImageAdapter.ProductImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductImageViewHolder {
        // Necesitamos un layout simple para cada página del ViewPager
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_image_slide, parent, false)
        return ProductImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductImageViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = products.size

    class ProductImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_product_slide)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_product_name_slide)
        private val priceTextView: TextView = itemView.findViewById(R.id.tv_product_price_slide)

        fun bind(product: ProductoPedido) {
            nameTextView.text = product.nombre
            priceTextView.text = String.format("S/. %.2f c/u", product.precioUnitario)
            Glide.with(itemView.context)
                .load(product.imageUrl)
                .into(imageView)
        }
    }
}
