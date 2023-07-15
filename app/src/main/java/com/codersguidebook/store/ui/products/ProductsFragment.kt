package com.codersguidebook.store.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.codersguidebook.store.MainActivity
import com.codersguidebook.store.StoreViewModel
import com.codersguidebook.store.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var adapter: ProductsAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val storeViewModel: StoreViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductsAdapter(mainActivity, this)
        binding.productsRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.productsRecyclerView.adapter = adapter

        storeViewModel.products.value?.let { adapter.products.addAll(it) }
        adapter.notifyItemRangeInserted(0, adapter.products.size)

        storeViewModel.currency.observe(viewLifecycleOwner) { currency ->
            currency?.let {
                binding.productsRecyclerView.visibility = View.VISIBLE
                binding.loadingProgress.visibility = View.GONE
                if (adapter.currency == null || currency.symbol != adapter.currency?.symbol) {
                    adapter.currency = currency
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
            }
        }
    }

    fun updateCart(index: Int) {
        adapter.products[index].inCart = !adapter.products[index].inCart
        adapter.notifyItemChanged(index)

        storeViewModel.products.value = adapter.products
        storeViewModel.calculateOrderTotal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}