package com.example.project_equal.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val userToken = sharedPreferences.getString("user_token", null)

        Log.d("MainActivity", "User Token: ${userToken}")
        if (userToken != null) {
            Log.d("MainActivity", "User Token: $userToken")
        } else {
            Log.d("MainActivity", "No login info found, redirecting to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onTouchEvent(event)
    }
}
