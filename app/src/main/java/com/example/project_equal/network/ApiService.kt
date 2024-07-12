package com.example.project_equal.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/login/")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}