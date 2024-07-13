package com.example.project_equal.network

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access")
    val accessToken: String,

    @SerializedName("refresh")
    val refreshToken: String
)
