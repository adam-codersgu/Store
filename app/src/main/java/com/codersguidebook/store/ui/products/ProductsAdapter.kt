package com.codersguidebook.store.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.codersguidebook.store.MainActivity
import com.codersguidebook.store.Product
import com.codersguidebook.store.R

class ProductsAdapter(private val activity: MainActivity, private val fragment: ProductsFragment) : RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>() {
    val products = mutableListOf<Product>()

    inner class ProductsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        internal var productImage = itemView.findViewById<ImageView>(R.id.productImage)
        internal var productName = itemView.findViewById<TextView>(R.id.productName)
        internal var productPrice = itemView.findViewById<TextView>(R.id.productPrice)
        internal var addToBasketButton = itemView.findViewById<Button>(R.id.addToBasketButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        return ProductsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product, parent, false))
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val current = products[position]

        Glide.with(activity)
            .load(current.image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .override(600, 600)
            .into(holder.productImage)

        holder.productName.text = current.name

        // TODO: Set the price of the product here

        if (current.inCart) {
            holder.addToBasketButton.text = activity.resources.getString(R.string.remove_from_basket)
            holder.addToBasketButton.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark))
        } else {
            holder.addToBasketButton.text = activity.resources.getString(R.string.add_to_basket)
            holder.addToBasketButton.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.holo_green_dark))
        }

        holder.addToBasketButton.setOnClickListener {
            fragment.updateCart(position)
        }
    }

    override fun getItemCount() = products.size
}
