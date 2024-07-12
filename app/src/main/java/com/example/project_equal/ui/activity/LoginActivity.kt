package com.example.project_equal.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.LoginRequest
import com.example.project_equal.network.LoginResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        val client = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://52.78.68.85:8000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val userIdInput: EditText = findViewById(R.id.inputId)
            val passwordInput: EditText = findViewById(R.id.inputPassword)
            val userid = userIdInput.text.toString()
            val password = passwordInput.text.toString()

            val loginRequest = LoginRequest(userid = userid, password = password)

            Log.d("LoginActivity", "Sending login request: $loginRequest")

            apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Log.d("LoginActivity", "Login successful: ${loginResponse?.user_id}")
                        Toast.makeText(this@LoginActivity, "Login successful: ${loginResponse?.user_id}", Toast.LENGTH_LONG).show()

                        // Save login info to SharedPreferences
                        val editor = sharedPreferences.edit()
                        editor.putString("user_id", loginResponse?.user_id)
                        editor.apply()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e("LoginActivity", "Login failed with status code: ${response.message()}")
                        Toast.makeText(this@LoginActivity, "Login failed with status code: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LoginActivity", "Login failed: ${t.message}")
                    Toast.makeText(this@LoginActivity, "Login failed: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        val signUpButton: Button = findViewById(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
