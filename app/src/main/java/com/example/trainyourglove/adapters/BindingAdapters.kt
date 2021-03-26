package com.example.trainyourglove.adapters

import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter

@BindingAdapter("app:tint")
fun ImageView.setImageTint(@ColorInt color: Int) {
    setColorFilter(color)
}