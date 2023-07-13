package com.codersguidebook.store

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StoreViewModel : ViewModel() {
    var products = MutableLiveData<List<Product>>()
}
