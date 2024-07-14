package com.example.project_equal.ui.activity

import android.content.ClipData
import android.content.ClipDescription
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
import com.example.project_equal.Expression
import com.example.project_equal.Operator
import com.example.project_equal.R

class ProblemActivity : AppCompatActivity() {

    private val TAG = "ProblemActivity"
    private lateinit var deleteButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)

        val problemTextView: TextView = findViewById(R.id.problem_text)
        resultTextView = findViewById(R.id.result_text)
        deleteButton = findViewById(R.id.delete_button)

        // Initialize buttons
        val plusButton: Button = findViewById(R.id.plus_button)
        val minusButton: Button = findViewById(R.id.minus_button)
        val multiplyButton: Button = findViewById(R.id.multiply_button)
        val divideButton: Button = findViewById(R.id.divide_button)

        // Set button listeners
        plusButton.setOnClickListener { createDraggableItem(Operator.Addition) }
        minusButton.setOnClickListener { createDraggableItem(Operator.Subtraction) }
        multiplyButton.setOnClickListener { createDraggableItem(Operator.Multiplication) }
        divideButton.setOnClickListener { createDraggableItem(Operator.Division) }

        val layout = findViewById<RelativeLayout>(R.id.root_layout)
        layout.setOnDragListener(dragListener)

        // Create draggable numbers
        createDraggableNumber(1)
        createDraggableNumber(2)
        createDraggableNumber(3)
        createDraggableNumber(4)
    }

    private fun createDraggableNumber(number: Int) {
        val draggableItem = TextView(this).apply {
            text = number.toString()
            textSize = 20f
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.color.holo_green_light)
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val clipText = text
                    val item = ClipData.Item(clipText)
                    val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    val dragData = ClipData(clipText, mimeTypes, item)
                    val dragShadow = View.DragShadowBuilder(view)

                    view.startDragAndDrop(dragData, dragShadow, view, 0)
                    view.visibility = View.INVISIBLE
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
            Log.d(TAG, "Draggable number created: ${draggableItem.text}")
        }
    }

    private fun createDraggableItem(operator: Operator) {
        val draggableItem = TextView(this).apply {
            text = when (operator) {
                is Operator.Addition -> "[] + []"
                is Operator.Subtraction -> "[] - []"
                is Operator.Multiplication -> "[] * []"
                is Operator.Division -> "[] / []"
                else -> "[]"
            }
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
            Log.d(TAG, "Draggable item created: ${draggableItem.text}")
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

                if (x >= deleteButtonX && x <= deleteButtonX + deleteButtonWidth &&
                    y >= deleteButtonY && y <= deleteButtonY + deleteButtonHeight) {
                    Log.d(TAG, "View deleted")
                } else {
                    val layout = v as ViewGroup
                    val overlappingView = findOverlappingView(x, y, draggedView, layout)
                    if (overlappingView != null && overlappingView is TextView && isOperator(overlappingView)) {
                        // Update the operator view with the dragged number
                        updateOperatorView(overlappingView, draggedView as TextView)
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

    private fun isOperator(view: TextView): Boolean {
        val text = view.text.toString()
        return text.contains("[] + []") || text.contains("[] - []") ||
                text.contains("[] * []") || text.contains("[] / []")
    }

    private fun updateOperatorView(operatorView: TextView, numberView: TextView) {
        val operatorText = operatorView.text.toString()
        val numberText = numberView.text.toString()

        val updatedText = when {
            operatorText.startsWith("[]") -> operatorText.replaceFirst("[]", numberText)
            operatorText.endsWith("[]") -> operatorText.replaceFirst("[]", numberText)
            else -> operatorText
        }

        operatorView.text = updatedText
        Log.d(TAG, "Updated operator view: $updatedText")
    }
}
