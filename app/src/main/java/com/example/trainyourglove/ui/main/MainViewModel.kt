package com.example.trainyourglove.ui.main

import android.app.Application
import androidx.lifecycle.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _index = MutableLiveData<Int>()

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun getIndex(): Int {
        return if (_index.value == null) 0 else _index.value!!
    }
}