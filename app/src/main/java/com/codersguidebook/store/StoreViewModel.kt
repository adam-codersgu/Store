package com.codersguidebook.store

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StoreViewModel : ViewModel() {
    var currency = MutableLiveData<Currency?>(null)
    var products = MutableLiveData<List<Product>>()
}
