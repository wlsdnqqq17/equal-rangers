package com.example.project_equal.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/login")
    fun login(@Body login: LoginRequest): Call<LoginResponse>
}