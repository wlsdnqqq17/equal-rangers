package com.example.project_equal.network

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("refresh") val refreshToken: String
)
