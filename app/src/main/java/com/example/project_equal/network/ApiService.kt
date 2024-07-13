package com.example.project_equal.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/api/token/")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/signup/")
    fun signup(@Body request: SignUpRequest): Call<SignUpResponse>

    @POST("/api/token/refresh/")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>


    suspend fun getPlayerInfo(
        @Header("Authorization") authorization: String,
        @Path("user_id") user_id: String
    ): PlayerData
}