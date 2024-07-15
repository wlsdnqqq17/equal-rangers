package com.example.project_equal.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowInsetsAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project_equal.R
import com.example.project_equal.network.ApiService
import com.example.project_equal.network.NumbersRequest
import com.example.project_equal.network.Problem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Random
import PlayerManager
import android.app.VoiceInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ThreeChoiceActivity : AppCompatActivity() {
    val problem_history = mutableListOf<String>()
    val num_problems = 7847
    lateinit var btn1 : TextView
    lateinit var btn2 : TextView
    lateinit var btn3 : TextView
    lateinit var playerManager : PlayerManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_three_choice)

        btn1 = findViewById(R.id.choice1Button)
        btn2 = findViewById(R.id.choice2Button)
        btn3 = findViewById(R.id.choice3Button)
        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("http://52.78.68.85:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        playerManager = PlayerManager(apiService, this)

        fetchProblems()

        setupButtonListeners()
    }
    private fun fetchProblems() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                val accessToken = playerManager.getAccessToken(sharedPreferences)

//                // Retrofit에 인터셉터 추가
//                val client = OkHttpClient.Builder()
//                    .addInterceptor { chain ->
//                        val original = chain.request()
//                        val request: Request = original.newBuilder()
//                            .header("Authorization", "Bearer $accessToken")
//                            .build()
//                        chain.proceed(request)
//                    }
//                    .build()
//
//                val retrofit = Retrofit.Builder()
//                    .baseUrl("http://52.78.68.85:8000")
//                    .client(client)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build()
//
//                val apiServiceWithAuth = retrofit.create(ApiService::class.java)

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
//                    if()
//                    Log.e("ThreeChoiceActivity", "Failed to fetch problems: ${response.code()}")
//                    null
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
            startActivity(intent)
        }
        btn2.setOnClickListener {
            val intent = Intent(this, ProblemActivity::class.java)
            intent.putExtra("PROBLEM_NUMBER", btn2.text)
            problem_history.add(btn2.text.toString())
            startActivity(intent)
        }
        btn3.setOnClickListener {
            val intent = Intent(this, ProblemActivity::class.java)
            intent.putExtra("PROBLEM_NUMBER", btn3.text)
            problem_history.add(btn3.text.toString())
            startActivity(intent)
        }
    }

    override fun onResume(){
        super.onResume()
        fetchProblems()
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://52.78.68.85:8000")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        // ApiService 인터페이스 구현체 생성
//
//
//        val apiService = retrofit.create(ApiService::class.java)
//
//        val random = Random()
//        val number1 = random.nextInt(num_problems) + 1 // 0부터 99까지의 랜덤 숫자 생성
//        val number2 = random.nextInt(num_problems) + 1
//        val number3 = random.nextInt(num_problems) + 1
//
//        val ids = "$number1,$number2,$number3"
//
//        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
//
//
//
//        val call = apiService.getProblems(ids)
//        call.enqueue(object : Callback<List<Problem>> {
//            override fun onResponse(call: Call<List<Problem>>, response: Response<List<Problem>>) {
//                if (response.isSuccessful) {
//                    val problems = response.body()
//                    Log.d("EST", "$response, ${response.body()}")
//                    if (problems != null) {
//                        btn1.text = problems[0].num
//                        btn2.text = problems[1].num
//                        btn3.text = problems[2].num
//                    }
//                } else {
//                    Log.e("ThreeChoiceActivity", "Failed to fetch problems: ${response.code()}")
//                }
//            }
//
//            override fun onFailure(call: Call<List<Problem>>, t: Throwable) {
//                Log.e("ThreeChoiceActivity", "Network error: ${t.message}")
//            }
//        })

    }
}