package com.example.project_equal.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val problem1: TextView = findViewById(R.id.problem1)
        val problem2: TextView = findViewById(R.id.problem2)
        val problem3: TextView = findViewById(R.id.problem3)

        problem1.setOnClickListener { openProblemActivity(problem1.text.toString()) }
        problem2.setOnClickListener { openProblemActivity(problem2.text.toString()) }
        problem3.setOnClickListener { openProblemActivity(problem3.text.toString()) }
    }

    private fun openProblemActivity(problemNumber: String) {
        Log.d("GameActivity", "Opening ProblemActivity with problem number: $problemNumber")
        val intent = Intent(this, ProblemActivity::class.java)
        intent.putExtra("PROBLEM_NUMBER", problemNumber)
        startActivity(intent)
    }
}
