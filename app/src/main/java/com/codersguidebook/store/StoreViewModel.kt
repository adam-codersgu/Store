package com.codersguidebook.store

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.math.RoundingMode

class StoreViewModel : ViewModel() {
    var currency = MutableLiveData<Currency?>(null)
    var orderTotal = MutableLiveData(0.00)
    var products = MutableLiveData<List<Product>>()

    fun calculateOrderTotal() {
        val basket = products.value?.filter { product ->
            product.inCart
        } ?: listOf()

        var total = basket.sumOf { product -> product.price }
        if (currency.value != null) total *= currency.value?.exchangeRate ?: 1.00

        orderTotal.value = BigDecimal(total).setScale(2, RoundingMode.HALF_EVEN).toDouble()
    }
}
