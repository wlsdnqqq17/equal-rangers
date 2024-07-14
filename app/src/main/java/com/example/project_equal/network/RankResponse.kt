package com.example.project_equal.network

import com.google.gson.annotations.SerializedName

class RankResponse(
    val id: Int,
    @SerializedName("user_id") val user_id: String,
    @SerializedName("score") val score: Int
)