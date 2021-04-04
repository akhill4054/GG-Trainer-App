package com.example.trainyourglove.data.models

import com.google.gson.annotations.SerializedName

class PostReset(
    @SerializedName("pass_key")
    val pass: String
)