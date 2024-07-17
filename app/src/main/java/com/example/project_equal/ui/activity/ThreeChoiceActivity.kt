package com.example.project_equal.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.Problem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Random
import PlayerManager
import android.graphics.Color
import android.widget.Button

class ThreeChoiceActivity : AppCompatActivity() {
    val problem_history = mutableListOf<String>()
    val num_problems = 7847
    lateinit var btn1: TextView
    lateinit var btn2: TextView
    lateinit var btn3: TextView
    lateinit var timeview: TextView
    lateinit var nextButton: Button
    lateinit var playerManager: PlayerManager
    var score: Int = 0
    var gold:Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private var remainingTime = TIMEOUT_DURATION
    private val timeoutRunnable = Runnable {
        navigateToGameResult()
    }
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (remainingTime > 0) {
                remainingTime -= 1000
                nextButton.text = "Next (${remainingTime / 1000}s)"
                handler.postDelayed(this, 1000)
            } else {
                navigateToGameResult()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PROBLEM = 1
        const val TIMEOUT_DURATION = 60000L // 60 seconds in milliseconds
        const val TIME_ADJUSTMENT = 5000L // 5 seconds in milliseconds

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_three_choice)

        btn1 = findViewById(R.id.choice1Button)
        btn2 = findViewById(R.id.choice2Button)
        btn3 = findViewById(R.id.choice3Button)
        timeview = findViewById(R.id.time_view)
        nextButton = findViewById(R.id.next_button)

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("http://52.78.68.85:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        playerManager = PlayerManager(apiService, this)

        fetchProblems()
        setupButtonListeners()

        nextButton.setOnClickListener {
            navigateToGameResult()
        }
    }

    private fun fetchProblems() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                val accessToken = playerManager.getAccessToken(sharedPreferences)

                val random = Random()
                val number1 = random.nextInt(num_problems) + 1
                val number2 = random.nextInt(num_problems) + 1
                val number3 = random.nextInt(num_problems) + 1
                val ids = "$number1,$number2,$number3"

                val problems = getProblems(accessToken, ids)
                if (problems != null) {
                    btn1.text = problems[0].num
                    btn2.text = problems[1].num
                    btn3.text = problems[2].num
                }
            } catch (e: Exception) {
                Log.e("ThreeChoiceActivity", "Error fetching problems", e)
            }
        }
    }

    private suspend fun getProblems(accessToken: String, ids: String): List<Problem>? {
        return withContext(Dispatchers.IO) {
            try {
                // Retrofit에 인터셉터 추가
                val client = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val request: Request = original.newBuilder()
                            .header("Authorization", "Bearer $accessToken")
                            .build()
                        chain.proceed(request)
                    }
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("http://52.78.68.85:8000")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val apiServiceWithAuth = retrofit.create(ApiService::class.java)
                val response = apiServiceWithAuth.getProblems(ids).execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    if (response.code() == 401) {
                        // 액세스 토큰이 만료된 경우 토큰 갱신 시도
                        Log.d("PlayerManager", "Access token expired. Refreshing token...")
                        val newAccessToken = playerManager.refreshAccessToken()
                        return@withContext getProblems(newAccessToken, ids)
                    } else {
                        throw IOException("Unexpected code $response")
                    }
                }
            } catch (e: Exception) {
                Log.e("ThreeChoiceActivity", "Network error: ${e.message}")
                null
            }
        }
    }

    private fun setupButtonListeners() {
        btn1.setOnClickListener {
            val intent = Intent(this, ProblemActivity::class.java)
            intent.putExtra("PROBLEM_NUMBER", btn1.text.toString())
            problem_history.add(btn1.text.toString())
            startActivityForResult(intent, REQUEST_CODE_PROBLEM)
            adjustTime(-TIME_ADJUSTMENT)
        }
        btn2.setOnClickListener {
            val intent = Intent(this, ProblemActivity::class.java)
            intent.putExtra("PROBLEM_NUMBER", btn2.text.toString())
            problem_history.add(btn2.text.toString())
            startActivityForResult(intent, REQUEST_CODE_PROBLEM)
            adjustTime(-TIME_ADJUSTMENT)
        }
        btn3.setOnClickListener {
            val intent = Intent(this, ProblemActivity::class.java)
            intent.putExtra("PROBLEM_NUMBER", btn3.text.toString())
            problem_history.add(btn3.text.toString())
            startActivityForResult(intent, REQUEST_CODE_PROBLEM)
            adjustTime(-TIME_ADJUSTMENT)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchProblems()
        handler.postDelayed(timeoutRunnable, TIMEOUT_DURATION)
        handler.post(updateTimeRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timeoutRunnable)
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PROBLEM && resultCode == RESULT_OK) {
            val scoreGain = data?.getIntegerArrayListExtra("SCORE")!!
            score += scoreGain[0]
            gold += scoreGain[1]
            adjustTime(TIME_ADJUSTMENT)
            Toast.makeText(this, "Score: $score, Gold:$gold", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToGameResult() {
        val intent = Intent(this, GameResult::class.java)
        intent.putIntegerArrayListExtra("PROBLEM_RESULT", arrayListOf(score, gold))
        startActivity(intent)
        finish()
    }
    private fun adjustTime(amount: Long) {
        remainingTime += amount
        if (remainingTime < 0) {
            remainingTime = 0
        }
        if (remainingTime <= 5000) {
            timeview.setTextColor(Color.RED)
        }
        timeview.text = "Time: ${remainingTime / 1000}"
        nextButton.text = "Next (${remainingTime / 1000}s)"
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, remainingTime)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티를 종료하여 BackStack에서 제거
    }
}
