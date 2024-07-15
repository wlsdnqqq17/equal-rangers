package com.example.project_equal.network

import com.google.gson.annotations.SerializedName

class Problem (
    @SerializedName("num") val num: String,
    @SerializedName("difficulty") val difficulty: Int
)