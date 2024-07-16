package com.example.project_equal.ui.activity

import PlayerManager
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory

class GameResult : AppCompatActivity() {
    private lateinit var playerManager: PlayerManager
    private lateinit var userId: String
    private lateinit var accessToken: String
    private var score: Int = 0
    private var gainGold: Int = 0
    private lateinit var scoreTextView: TextView
    private lateinit var goldTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_result)

        // Intent에서 점수를 가져옴
        val data = intent.getIntegerArrayListExtra("PROBLEM_RESULT")!!
        score = data[0]
        gainGold = data[1]
        //score = 100
        //gainGold = 100

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
        scoreTextView = findViewById(R.id.scoreTextView)
        goldTextView = findViewById(R.id.goldTextView)

        // ApiService 인터페이스 구현체 생성
        val apiService = retrofit.create(ApiService::class.java)

        // PlayerManager 인스턴스 생성
        playerManager = PlayerManager(apiService, this)

        // 플레이어 정보 업데이트
        updatePlayerinfo(apiService)
        updateGold(gainGold)
        animateScore(0, score)
    }

    private fun updateGold(gold: Int) {
        goldTextView.text = "Gold: $gold"
    }

    private fun animateScore(startScore: Int, endScore: Int) {
        val animator = ValueAnimator.ofInt(startScore, endScore)
        animator.duration = 2000 // 2초 동안 애니메이션

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            scoreTextView.text = "Score: $animatedValue"
        }

        animator.start()
    }

    private fun updatePlayerinfo(apiService: ApiService) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val playerData = playerManager.getPlayerInfo(userId, accessToken)
                val updatedPlayerData = playerData.copy(highscore = score, gold = playerData.gold + gainGold)
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
                    if (response.code() == 403 || response.code() == 401) {
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티를 종료하여 BackStack에서 제거
    }
}
