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
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var playerManager: PlayerManager

    val base_url = "http://52.78.68.85:8000"

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
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(base_url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        playerManager = PlayerManager(apiService, this)

        val logoutButton = findViewById<Button>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                logout()
            }
        }
        val startGameButton = findViewById<Button>(R.id.btn_start_game)
        startGameButton.setOnClickListener {
            val intent = Intent(this, ThreeChoiceActivity::class.java)
            startActivity(intent)
        }

        user_id?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val accessToken = getAccessToken()
                    Log.d("IN HOMEACTIVITY", accessToken)
                    getPlayerInfo(it)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeActivity, "Failed to fetch player information.", Toast.LENGTH_SHORT).show()
                        Log.e("HomeActivity", "Error: ${e.message}")
                    }
                }
            }
        } ?: run {
            Toast.makeText(this@HomeActivity, "User ID not found.", Toast.LENGTH_SHORT).show()
        }
    }

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

    private suspend fun getPlayerInfo(userId: String) {
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

    private suspend fun updateUI(playerData: PlayerData) {
        withContext(Dispatchers.Main) {
            userIdTextView.text = playerData.nickname
            goldTextView.text = playerData.gold.toString()
            highscoreTextView.text = playerData.highscore.toString()
            Log.d("updateUI", "username: ${playerData.userId}, nickname: ${playerData.nickname}, email: ${playerData.email}")
        }
    }

    private suspend fun logout() {
        try {
            val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val tokensJsonString = sharedPreferences.getString("user_token", null)
            val tokensJson = JSONObject(tokensJsonString ?: throw IOException("User token not found or is null"))
            val refreshToken = tokensJson.getString("refresh")
            Log.d("Logout", "$tokensJson")
            val logoutRequest = LogoutRequest(refreshToken)
            val response: Response<Void> = apiService.logout(logoutRequest).execute()

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
