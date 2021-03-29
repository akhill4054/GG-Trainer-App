package com.example.trainyourglove.adapters

import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter

@BindingAdapter("app:tint")
fun ImageView.setImageTint(@ColorInt color: Int) {
    setColorFilter(color)
}

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