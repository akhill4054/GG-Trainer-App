package com.example.trainyourglove.adapters

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("app:isVisible")
fun View.isVisible(isVisible: Boolean) {
    visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("app:isInvisible")
fun View.isInvisible(isInvisible: Boolean) {
    visibility = if (isInvisible) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
}