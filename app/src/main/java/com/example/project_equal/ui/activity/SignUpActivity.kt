package com.example.project_equal.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.SignUpRequest
import com.example.project_equal.network.SignUpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignUpActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://52.78.68.85:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        val signUpButton: ImageButton = findViewById(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val signUpId: EditText = findViewById(R.id.signUpId)
            val signUpPassword: EditText = findViewById(R.id.signUpPassword)
            val signUpPasswordConfirm: EditText = findViewById(R.id.signUpPasswordConfirm)
            val signUpNickname: EditText = findViewById(R.id.signUpNickname)
            val signUpLastName: EditText = findViewById(R.id.signUpLastName)
            val signUpFirstName: EditText = findViewById(R.id.signUpFirstName)

            val userid = signUpId.text.toString()
            val password = signUpPassword.text.toString()
            val passwordConfirm = signUpPasswordConfirm.text.toString()
            val nickname = signUpNickname.text.toString()
            val last_name = signUpLastName.text.toString()
            val first_name = signUpFirstName.text.toString()

            val signUpRequest = SignUpRequest(
                username = userid,
                password = password,
                password_confirm = passwordConfirm,
                nickname = nickname,
                last_name = last_name,
                first_name = first_name
            )

            apiService.signup(signUpRequest).enqueue(object : Callback<SignUpResponse> {
                override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                    if (response.isSuccessful) {
                        val signUpResponse = response.body()
                        Log.d("SignUpActivity", "Sign up successful: ${signUpResponse?.user_id}")

                        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e("SignUpActivity", "Sign up failed with status code: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    Log.e("SignUpActivity", "Sign up failed: ${t.message}")
                }
            })
        }
    }
}
