package com.example.project_equal.ui.activity

import PlayerManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.LogoutRequest
import com.example.project_equal.network.PlayerData
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var playerManager: PlayerManager

    val base_url = "http://52.78.68.85:8000"
    val refresh_url = "$base_url/api/token/refresh/"

    private lateinit var userIdTextView: TextView
    private lateinit var goldTextView: TextView
    private lateinit var highscoreTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        userIdTextView = findViewById(R.id.userid_text)
        goldTextView = findViewById(R.id.gold_text)
        highscoreTextView = findViewById(R.id.highscore)

        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val user_id = sharedPreferences.getString("user_id", null)
        Log.d("LOGIN ID CHECK", user_id!!)
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(base_url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        playerManager = PlayerManager(apiService, this)
        // CoroutineScope를 사용하여 비동기로 데이터를 가져오고 UI를 업데이트합니다.

        val logoutButton = findViewById<Button>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                logout()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getAccessToken()
                Log.d("IN HOMEACTIVITY", accessToken)
                val playerData = getPlayerInfo(user_id)
//                updateUI(playerData)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Failed to fetch player information.", Toast.LENGTH_SHORT).show()
                    Log.e("HomeActivity", "Error: ${e.message}")
                }
            }
        }
    }

    // Access Token 가져오기
    private suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            try {
                val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                val tokensJsonString = sharedPreferences.getString("user_token", null)
                val tokensJson = JSONObject(tokensJsonString ?: throw IOException("User token not found or is null"))

                tokensJson.getString("access")
            } catch (e: IOException) {
                throw IOException("Failed to read tokens.json")
            }
        }
    }

    private suspend fun getPlayerInfo(userId: String){
        try {
            val accessToken = getAccessToken()
            val playerData = playerManager.getPlayerInfo(userId, accessToken)
            updateUI(playerData)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeActivity, "Failed to fetch player information.", Toast.LENGTH_SHORT).show()
                Log.e("HomeActivity", "Error: ${e.message}")
            }
        }
    }

    // UI 업데이트
    private suspend fun updateUI(playerData: PlayerData) {
        withContext(Dispatchers.Main) {
            val username = playerData.userId
            val nickname = playerData.nickname
            val email = playerData.email
            val gold = playerData.gold
            val highscore = playerData.highscore
            userIdTextView.text = playerData.nickname
            goldTextView.text = playerData.gold.toString()
            highscoreTextView.text = playerData.highscore.toString()
            Log.d("updateUI", "username: $username, nickname: $nickname, email:$email")

        }
    }

    private suspend fun logout() {
        try {
            val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val tokensJsonString = sharedPreferences.getString("user_token", null)
            val tokensJson = JSONObject(tokensJsonString ?: throw IOException("User token not found or is null"))
            val refreshToken = tokensJson.getString("refresh")
            Log.d("Logout", "$tokensJson");
            val logoutRequest = LogoutRequest(refreshToken)
            val response: Response<Void> = apiService.logout(logoutRequest).execute()

//            if (!response.isSuccessful) {
//                if (response.code == 401) {
//                    // 액세스 토큰이 만료된 경우 토큰 갱신 시도
//                    Log.d("PlayerManager", "Access token expired. Refreshing token...")
//                    val newAccessToken = refreshAccessToken()
//                    return@withContext getPlayerInfo(userId, newAccessToken)
//                } else {
//                    throw IOException("Unexpected code $response")
//                }
//            }


            if (response.isSuccessful) {
                clearTokens()
                val loginIntent = Intent(this@HomeActivity, LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(loginIntent)
                finish()
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Failed to log out.", Toast.LENGTH_SHORT).show()
                    Log.e("HomeActivity", "Error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeActivity, "Failed to log out: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeActivity", "Error: ${e.message}")
            }
        }
    }
    private fun clearTokens() {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}
