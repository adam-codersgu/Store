package com.codersguidebook.store.ui.checkout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.codersguidebook.store.Currency
import com.codersguidebook.store.MainActivity
import com.codersguidebook.store.Product
import com.codersguidebook.store.R

class CheckoutAdapter(private val activity: MainActivity, private val fragment: CheckoutFragment) : RecyclerView.Adapter<CheckoutAdapter.ProductsViewHolder>() {
    var products = mutableListOf<Product>()
    var currency: Currency? = null

    inner class ProductsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        internal var productImage = itemView.findViewById<ImageView>(R.id.productImage)
        internal var productName = itemView.findViewById<TextView>(R.id.productName)
        internal var productPrice = itemView.findViewById<TextView>(R.id.productPrice)
        internal var removeFromBasketButton = itemView.findViewById<ImageButton>(R.id.removeFromBasketButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        return ProductsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.basket_product, parent, false))
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val current = products[position]

        Glide.with(activity)
            .load(current.image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .override(400, 400)
            .into(holder.productImage)

        holder.productName.text = current.name
        val price = if (currency?.exchangeRate == null) current.price
        else current.price * currency?.exchangeRate!!

        holder.productPrice.text = activity.resources.getString(R.string.product_price, currency?.symbol, String.format("%.2f", price))

        holder.removeFromBasketButton.setOnClickListener {
            fragment.removeProduct(current)
        }
    }

    override fun getItemCount() = products.size
}
