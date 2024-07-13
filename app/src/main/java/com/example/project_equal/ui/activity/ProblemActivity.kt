package com.example.project_equal.ui.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R

class ProblemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)
        // 인텐트에서 문제 텍스트를 가져와 표시합니다.
        val problemText = intent.getCharSequenceExtra("PROBLEM_TEXT").toString()
        val problemTextView: TextView = findViewById(R.id.problem_text)
        problemTextView.text = problemText
    }
}
