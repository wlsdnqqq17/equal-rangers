package com.example.project_equal.ui.activity

import PlayerManager
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_equal.R
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.LogoutRequest
import com.example.project_equal.network.PlayerData
import com.example.project_equal.network.RankData
import com.example.project_equal.network.RankingAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Random
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var playerManager: PlayerManager

    val base_url = "http://52.78.68.85:8000"

    private lateinit var userIdTextView: TextView
    private lateinit var goldTextView: TextView
    private lateinit var highscoreTextView: TextView
    private lateinit var btnShowRanking: Button
    private lateinit var random: Random
    private lateinit var items : MutableList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        random = Random()
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

        btnShowRanking = findViewById(R.id.btn_show_ranking)
        btnShowRanking.setOnClickListener {
            showRankingDialog()
        }

        user_id?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val accessToken = getAccessToken()
                    Log.d("IN HOMEACTIVITY", accessToken)
                    getPlayerInfo(it)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@HomeActivity,
                            "Failed to fetch player information.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("HomeActivity", "Error: ${e.message}")
                    }
                }
            }
        } ?: run {
            Toast.makeText(this@HomeActivity, "User ID not found.", Toast.LENGTH_SHORT).show()
        }
//
//        val characterContainer = findViewById<RelativeLayout>(R.id.characterContainer)
////        val characters = items
//        for (i in 0 until items.size) {
//            addCharacter(characterContainer, i, items[i])
//        }
    }

    private fun showRankingDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.ranking_bottom_sheet, null)
        dialog.setContentView(dialogView)

        val rankingList = dialogView.findViewById<RecyclerView>(R.id.ranking_list)
        rankingList.layoutManager = LinearLayoutManager(this)

        dialog.show()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val rankings = fetchRankings()
                rankingList.adapter = RankingAdapter(rankings)
            } catch (e: Exception) {
                // Handle error
                Log.e("HomeActivity", "Failed to fetch rankings: ${e.message}")
                Toast.makeText(this@HomeActivity, "Failed to fetch rankings", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private suspend fun fetchRankings(): List<RankData> {
        return withContext(Dispatchers.IO) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://52.78.68.85:8000")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiService = retrofit.create(ApiService::class.java)
                val response = apiService.getRankings().execute()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    // Handle error here
                    emptyList()
                }
            } catch (e: Exception) {
                // Handle error here
                emptyList()
            }
        }
    }

    private suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            try {
                val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                val tokensJsonString = sharedPreferences.getString("user_token", null)
                val tokensJson = JSONObject(
                    tokensJsonString ?: throw IOException("User token not found or is null")
                )

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
                Toast.makeText(
                    this@HomeActivity,
                    "Failed to fetch player information.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("HomeActivity", "Error: ${e.message}")
            }
        }
    }

    private suspend fun updateUI(playerData: PlayerData) {
        withContext(Dispatchers.Main) {
            userIdTextView.text = playerData.nickname
            goldTextView.text = playerData.gold.toString()
            highscoreTextView.text = playerData.highscore.toString()
            Log.d("PLAYERDATA", "${playerData.item}")
            items = playerData.item.toMutableList()
            setAnimation(items)
            Log.d(
                "updateUI",
                "username: ${playerData.userId}, nickname: ${playerData.nickname}"
            )
        }
    }

    private suspend fun setAnimation(items: MutableList<Int>){
        val characterContainer = findViewById<RelativeLayout>(R.id.characterContainer)
//        val characters = items
        Log.d("test", "$items")
        for (i in 0 until items.size) {
            addCharacter(characterContainer, i, items[i])
        }
    }

    private suspend fun logout() {
        try {
            val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val tokensJsonString = sharedPreferences.getString("user_token", null)
            val tokensJson =
                JSONObject(tokensJsonString ?: throw IOException("User token not found or is null"))
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
                    Toast.makeText(this@HomeActivity, "Failed to log out.", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("HomeActivity", "Error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@HomeActivity,
                    "Failed to log out: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("HomeActivity", "Error: ${e.message}")
            }
        }
    }

    private fun setCharacter(character: ImageView,type: Int): FloatArray {
        var result = floatArrayOf(0f, 0f, 0f, 0f) // 점프 높이, 점프간 쉬는 시간, x축 점프 거리, 방향 바꿀 확률
        when(type) {
            0 -> {
                character.setImageResource(R.drawable.plus)
                result[0] = -150f
                result[1] = 500f
                result[2] = 100f
                result[3] = 500f
            }
            1 -> {
                character.setImageResource(R.drawable.minus)
                result[0] = -150f
                result[1] = 500f
                result[2] = 100f
                result[3] = 500f
            }
            2 -> {
                character.setImageResource(R.drawable.multiply)
                result[0] = -200f
                result[1] = 400f
                result[2] = 100f
                result[3] = 500f
            }
            3 -> {
                character.setImageResource(R.drawable.divide)
                result[0] = -500f
                result[1] = 50f
                result[2] = 100f
                result[3] = 800f
            }
            4 -> {
                character.setImageResource(R.drawable.root2)
                result[0] = -200f
                result[1] = 500f
                result[2] = 100f
                result[3] = 800f
            }
            5 -> {
                character.setImageResource(R.drawable.root3)
                result[0] = -200f
                result[1] = 500f
                result[2] = 100f
                result[3] = 800f
            }
            6 -> {
                character.setImageResource(R.drawable.power2)
                result[0] = -100f
                result[1] = 500f
                result[2] = 200f
                result[3] = 800f
            }
            7 -> {
                character.setImageResource(R.drawable.power3)
                result[0] = -100f
                result[1] = 500f
                result[2] = 200f
                result[3] = 800f
            }
            8 -> {
                character.setImageResource(R.drawable.colon)
                result[0] = -400f
                result[1] = 150f
                result[2] = 150f
                result[3] = 500f
            }
            9 -> {
                character.setImageResource(R.drawable.equal)
                result[0] = 0f
                result[1] = 500f
                result[2] = 100f
                result[3] = 500f
            }
        }
        return result
    }

    private fun addCharacter(container: RelativeLayout, index: Int, type: Int) {
        val character = ImageView(this)
        val param = setCharacter(character, type)
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val default_h = 150f
        val jump_height = default_h + param[0]
        val waitTime = param[1]
        var currentTranslationX = 0f
        var moveDistance = param[2] // 초기 이동 거리 설정
        val prob_change = param[3]

        params.width = 200 // 원하는 가로 크기 설정
        params.height = 200

        // 캐릭터를 배치할 위치 설정
        params.leftMargin = index * 300 // 각 캐릭터를 좌우로 배치
        params.addRule(RelativeLayout.CENTER_VERTICAL)

        character.layoutParams = params
        container.addView(character)

        val jumpUpAnimator = ObjectAnimator.ofFloat(character, "translationY", default_h, jump_height)
        jumpUpAnimator.duration = 300 // 애니메이션 지속 시간 (0.3초)
        jumpUpAnimator.interpolator = AccelerateDecelerateInterpolator()

        val jumpDownAnimator = ObjectAnimator.ofFloat(character, "translationY", jump_height, default_h)
        jumpDownAnimator.duration = 300 // 애니메이션 지속 시간 (0.3초)
        jumpDownAnimator.interpolator = AccelerateDecelerateInterpolator()

        val moveAnimatorX = ObjectAnimator.ofFloat(character, "translationX", currentTranslationX, currentTranslationX + moveDistance)
        moveAnimatorX.duration = 600

//        moveAnimatorX.addUpdateListener { animation ->
//            val value = animation.animatedValue as Float
//            character.scaleX = if (value > 0) 1f else -1f // 좌우 이동에 따라 캐릭터 방향 조정
//        }

        val jumpAnimatorSet = AnimatorSet()

        val pauseAnimator = ObjectAnimator.ofFloat(character, "translationY", default_h, default_h)
        pauseAnimator.duration = waitTime.toLong() // 멈춤 지속 시간 (0.5초)

        jumpAnimatorSet.playTogether(jumpUpAnimator, moveAnimatorX)
        jumpAnimatorSet.playSequentially(jumpUpAnimator, jumpDownAnimator, pauseAnimator)

        jumpAnimatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                val randomDuration = Random().nextInt(waitTime.toInt()) / 2 + waitTime
                currentTranslationX += moveDistance
                character.translationX = currentTranslationX

                // 이동 방향과 경계 처리
                if (random.nextInt(1000) < prob_change) {
                    moveDistance *= -1
                }

                // 화면 경계를 넘어가지 않도록 제한
                if (currentTranslationX > container.width - character.width) {
                    moveDistance = -abs(moveDistance)
                } else if (currentTranslationX < 0) {
                    moveDistance = abs(moveDistance)
                }
                if(moveDistance * character.scaleX < 0){
                    character.scaleX *= -1
                }

                moveAnimatorX.setFloatValues(currentTranslationX, currentTranslationX + moveDistance)
                pauseAnimator.duration = randomDuration.toLong()
                jumpAnimatorSet.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        jumpAnimatorSet.start()
    }



    private fun clearTokens() {
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}
