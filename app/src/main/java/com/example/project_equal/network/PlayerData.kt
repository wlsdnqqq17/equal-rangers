package com.example.project_equal.network

import com.google.gson.annotations.SerializedName

data class PlayerData(
    @SerializedName("user_id") val userId: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("gold") val gold : Int,
    @SerializedName("item") val item : List<Int>,
    @SerializedName("highscore") val highscore : Int
)