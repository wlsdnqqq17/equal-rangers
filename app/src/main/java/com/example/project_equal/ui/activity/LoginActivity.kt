package com.example.project_equal.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.LoginRequest
import com.example.project_equal.network.LoginResponse
import com.example.project_equal.ui.activity.MainActivity
import com.example.project_equal.ui.activity.SignUpActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class LoginActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://52.78.68.85:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        val loginButton: ImageButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val userIdInput: EditText = findViewById(R.id.inputId)
            val passwordInput: EditText = findViewById(R.id.inputPassword)
            val username = userIdInput.text.toString()
            val password = passwordInput.text.toString()

            val loginRequest = LoginRequest(username, password)

            Log.d("LoginActivity", "Sending login request: $loginRequest")

            apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            val tokensJson = JSONObject()
                            Log.d("LOG", "BODY IS : $loginResponse")
                            tokensJson.put("access", loginResponse.accessToken)
                            tokensJson.put("refresh", loginResponse.refreshToken)
                            val editor = sharedPreferences.edit()
//
                            val tokens = tokensJson.toString()
                            editor.putString("user_token", tokens)
                            editor.putString("user_id", username)
                            editor.apply()

                            Log.d("LoginActivity", "Tokens: $tokens")

                            // 토큰 저장
                            File(filesDir, "tokens.json").writeText(tokens)


                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish() // LoginActivity 종료
                        } else {
                            Log.e("LoginActivity", "Login response body is null")
                        }
                    } else {
                        Log.e("LoginActivity", "Login failed with status code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LoginActivity", "Login failed: ${t.message}")
                }
            })
        }

        val signUpButton: ImageButton = findViewById(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
