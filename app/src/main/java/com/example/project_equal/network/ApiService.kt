package com.example.project_equal.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/token/")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/signup/")
    fun signup(@Body request: SignUpRequest): Call<SignUpResponse>

}