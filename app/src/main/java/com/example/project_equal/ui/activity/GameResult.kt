package com.example.project_equal.ui.activity

import PlayerManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.RankData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class GameResult : AppCompatActivity() {
    private lateinit var playerManager: PlayerManager
    private lateinit var userId: String
    private lateinit var accessToken: String
    private var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_result)

        // Intent에서 점수를 가져옴
        score = intent.getIntExtra("PROBLEM_RESULT", 0)

        // SharedPreferences에서 userId와 accessToken 가져오기
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", "") ?: ""
        accessToken = sharedPreferences.getString("user_token", "") ?: ""

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("http://52.78.68.85:8000")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // ApiService 인터페이스 구현체 생성
        val apiService = retrofit.create(ApiService::class.java)

        // PlayerManager 인스턴스 생성
        playerManager = PlayerManager(apiService, this)

        // 플레이어 정보 업데이트
        updatePlayerHighscore(apiService)
    }

    private fun updatePlayerHighscore(apiService: ApiService) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val playerData = playerManager.getPlayerInfo(userId, accessToken)
                val updatedPlayerData = playerData.copy(highscore = score)
                val response = playerManager.updatePlayerInfo(userId, updatedPlayerData, accessToken)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GameResult, "Player highscore updated: ${response.highscore}", Toast.LENGTH_SHORT).show()
                }

                updateRank(apiService, accessToken)
            } catch (e: Exception) {
                Log.e("game_result", "Failed to update player highscore: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GameResult, "Failed to update player highscore", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private suspend fun updateRank(apiService: ApiService, accessToken: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val rankData = RankData(user_id = userId, score = score)
                val response = withContext(Dispatchers.IO) {
                    apiService.updateRank("Bearer $accessToken", rankData).awaitResponse()
                }

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GameResult, "Rank updated successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if(response.code() == 403){
                        Log.d("PlayerManager", "Access token expired. Refreshing token...")
                        val newAccessToken = playerManager.refreshAccessToken()
                        // Retry with the new access token
                        return@launch updateRank(apiService, newAccessToken)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GameResult, "Failed to update rank: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("game_result", "Failed to update rank: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GameResult, "Failed to update rank", Toast.LENGTH_SHORT).show()
                }
            }
        }
//        withContext(Dispatchers.IO) {
//            try {
//                val rankData = RankData(user_id = userId, score = score)
//                val response = apiService.updateRank("Bearer $accessToken", rankData).awaitResponse()
//
//                if (response.isSuccessful) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@GameResult, "Rank updated successfully", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@GameResult, "Failed to update rank: ${response.code()}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("game_result", "Failed to update rank: ${e.message}")
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@GameResult, "Failed to update rank", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
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
}
