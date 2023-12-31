package com.codersguidebook.store.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.codersguidebook.store.Currency
import com.codersguidebook.store.MainActivity
import com.codersguidebook.store.Product
import com.codersguidebook.store.R
import com.codersguidebook.store.StoreViewModel
import com.codersguidebook.store.databinding.FragmentCheckoutBinding

class CheckoutFragment : Fragment() {

    private var amount: Double? = null
    private var currency: Currency? = null
    private var _binding: FragmentCheckoutBinding? = null
    private val storeViewModel: StoreViewModel by activityViewModels()
    private lateinit var adapter: CheckoutAdapter
    private lateinit var mainActivity: MainActivity

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CheckoutAdapter(mainActivity, this)
        binding.cartRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.cartRecyclerView.adapter = adapter

        storeViewModel.products.observe(viewLifecycleOwner) { products ->
            val basket = products.filter { product -> product.inCart }

            if (basket.isEmpty()) binding.emptyCart.visibility = View.VISIBLE
            else binding.emptyCart.visibility = View.GONE

            when {
                basket.size > adapter.products.size -> {
                    val newProducts = basket - adapter.products.toSet()
                    for (product in newProducts) {
                        adapter.products.add(product)
                        adapter.notifyItemInserted(adapter.products.size - 1)
                    }
                }
                basket.size < adapter.products.size -> {
                    val removedProducts = adapter.products - basket.toSet()
                    for (product in removedProducts) {
                        val index = adapter.products.indexOf(product)
                        adapter.products.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                }
            }
            updateOrderTotal()
        }

        storeViewModel.orderTotal.observe(viewLifecycleOwner) { total ->
            this.amount = total
            updateOrderTotal()
        }

        storeViewModel.currency.observe(viewLifecycleOwner) { currency ->
            this.currency = currency
            if (currency?.symbol != adapter.currency?.symbol) {
                adapter.currency = currency
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
            }
            updateOrderTotal()
        }

        binding.payButton.setOnClickListener {
            mainActivity.initiatePayment()
        }
    }

    fun removeProduct(product: Product) {
        product.inCart = !product.inCart
        val products = storeViewModel.products.value?.toMutableList() ?: return
        val position = products.indexOf(product)
        if (position != -1) {
            products[position] = product
            storeViewModel.products.value = products
            storeViewModel.calculateOrderTotal()
        }
    }

    private fun updateOrderTotal() {
        if (currency == null || amount == null) return
        val total = currency!!.symbol + String.format("%.2f", amount)
        binding.orderTotal.text = resources.getString(R.string.order_total, total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}