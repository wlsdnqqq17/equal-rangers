package com.example.project_equal.ui.activity

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.R

class ProblemActivity : AppCompatActivity() {

    private val TAG = "ProblemActivity"
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)

        val problemNumber = intent.getStringExtra("PROBLEM_NUMBER")
        val problemTextView: TextView = findViewById(R.id.problem_text)
        problemTextView.text = problemNumber

        val plusButton: Button = findViewById(R.id.plus_button)
        val minusButton: Button = findViewById(R.id.minus_button)
        val multiplyButton: Button = findViewById(R.id.multiply_button)
        val divideButton: Button = findViewById(R.id.divide_button)

        val next_btn: Button = findViewById(R.id.next_button)
        deleteButton = findViewById(R.id.delete_button)

        plusButton.setOnClickListener { createDraggableItem("plus", "operator") }
        minusButton.setOnClickListener { createDraggableItem("minus", "operator") }
        multiplyButton.setOnClickListener { createDraggableItem("multiply", "operator") }
        divideButton.setOnClickListener { createDraggableItem("divide", "operator") }

        val layout = findViewById<RelativeLayout>(R.id.root_layout)
        layout.setOnDragListener(dragListener)

        // 네 자리 숫자를 드래그 가능한 아이템으로 생성
        problemNumber?.forEach { char ->
            if (char.isDigit()) {
                createDraggableItem(char.toString(), "number")
            }
        }
        next_btn.setOnClickListener(){
            val intent = Intent(this, GameResult::class.java)
            intent.putExtra("PROBLEM_RESULT", 10)
            startActivity(intent)
        }
    }

    private val dragListener = View.OnDragListener { v, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Log.d(TAG, "Drag started")
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.d(TAG, "Drag entered")
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED -> {
                Log.d(TAG, "Drag exited")
                true
            }
            DragEvent.ACTION_DROP -> {
                val draggedView = event.localState as View
                val owner = draggedView.parent as ViewGroup
                owner.removeView(draggedView)

                val x = event.x.toInt()
                val y = event.y.toInt()

                val deleteButtonLocation = IntArray(2)
                deleteButton.getLocationOnScreen(deleteButtonLocation)
                val deleteButtonX = deleteButtonLocation[0]
                val deleteButtonY = deleteButtonLocation[1]
                val deleteButtonWidth = deleteButton.width
                val deleteButtonHeight = deleteButton.height

                // 드래그된 뷰가 삭제 버튼 위에 있는지 확인
                if (x >= deleteButtonX && x <= deleteButtonX + deleteButtonWidth &&
                    y >= deleteButtonY && y <= deleteButtonY + deleteButtonHeight) {
                    Log.d(TAG, "View deleted")
                } else {
                    // 다른 뷰와 겹치는지 확인
                    val layout = v as ViewGroup
                    val overlappingView = findOverlappingView(x, y, draggedView, layout)
                    if (overlappingView != null) {
                        val newItem = createCombinedItem(draggedView, overlappingView)
                        layout.removeView(overlappingView)
                        layout.addView(newItem)
                        Log.d(TAG, "New combined view created")
                    } else {
                        val params = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.leftMargin = x - (draggedView.width / 2)
                        params.topMargin = y - (draggedView.height / 2)
                        layout.addView(draggedView, params)
                        draggedView.visibility = View.VISIBLE
                        Log.d(TAG, "View dropped at x: $x, y: $y")
                    }
                }
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                if (!event.result) {
                    val draggedView = event.localState as View
                    draggedView.visibility = View.VISIBLE
                }
                Log.d(TAG, "Drag ended")
                true
            }
            else -> false
        }
    }

    private fun findOverlappingView(x: Int, y: Int, draggedView: View, parent: ViewGroup): View? {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child != draggedView) {
                val location = IntArray(2)
                child.getLocationOnScreen(location)
                val childX = location[0]
                val childY = location[1]
                if (x >= childX && x <= childX + child.width &&
                    y >= childY && y <= childY + child.height) {
                    return child
                }
            }
        }
        return null
    }

    private fun createCombinedItem(view1: View, view2: View): View {
        val combinedText = (view1 as TextView).text.toString() + (view2 as TextView).text.toString()
        return TextView(this).apply {
            text = combinedText
            textSize = 20f
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.color.holo_green_light)
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val clipText = combinedText
                    val item = ClipData.Item(clipText)
                    val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    val dragData = ClipData(clipText, mimeTypes, item)
                    val dragShadow = View.DragShadowBuilder(view)

                    view.startDragAndDrop(dragData, dragShadow, view, 0)
                    view.visibility = View.INVISIBLE
                    Log.d(TAG, "Drag started for combined item: $combinedText")
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun createDraggableItem(text: String, type: String) {
        val draggableItem = TextView(this).apply {
            this.text = text
            this.tag = type
            textSize = 20f
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.color.holo_blue_light)
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val clipText = text
                    val item = ClipData.Item(clipText)
                    val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    val dragData = ClipData(clipText, mimeTypes, item)
                    val dragShadow = View.DragShadowBuilder(view)

                    view.startDragAndDrop(dragData, dragShadow, view, 0)
                    view.visibility = View.INVISIBLE
                    Log.d(TAG, "Drag started for item: $text")
                    true
                } else {
                    false
                }
            }
        }

        val layout = findViewById<RelativeLayout>(R.id.root_layout)
        layout.post {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            layout.addView(draggableItem, params)
            Log.d(TAG, "Draggable item created: $text")
        }
    }

}
