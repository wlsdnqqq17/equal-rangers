package com.example.project_equal.network

import com.google.gson.annotations.SerializedName

class RankData(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("score") val score: Int
)