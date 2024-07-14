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

        resultTextView = findViewById(R.id.result_text)
        deleteButton = findViewById(R.id.delete_button)

        // Initialize buttons
        val plusButton: Button = findViewById(R.id.plus_button)
        val minusButton: Button = findViewById(R.id.minus_button)
        val multiplyButton: Button = findViewById(R.id.multiply_button)
        val divideButton: Button = findViewById(R.id.divide_button)

        // Set button listeners
        plusButton.setOnClickListener { createDraggableItem(Operator.Addition()) }
        minusButton.setOnClickListener { createDraggableItem(Operator.Subtraction()) }
        multiplyButton.setOnClickListener { createDraggableItem(Operator.Multiplication()) }
        divideButton.setOnClickListener { createDraggableItem(Operator.Division()) }

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
                    view.tag = Expression.Number(number) // 태그에 Expression 객체 저장
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
            tag = operator // 각 드래거블이 독립적인 Operator 객체를 가짐
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
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED -> {
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
                    Log.d(TAG, "Overlapping view found: ${child.javaClass.simpleName}")
                    return child
                }
            }
        }
        return null
    }

    private fun isOperator(view: TextView): Boolean {
        val text = view.text.toString()
        return text.contains("+") || text.contains("-") ||
                text.contains("*") || text.contains("/") && text.contains("[]")
    }

    private fun updateOperatorView(operatorView: TextView, numberView: TextView) {
        val operator = operatorView.tag as? Operator ?: return
        val inputExpression = numberView.tag as? Expression ?: return
        Log.d(TAG, "inputExpression: $inputExpression")

        if (operator.leftExpression == null) {
            operator.leftExpression = inputExpression
        } else if (operator.rightExpression == null) {
            operator.rightExpression = inputExpression
        }

        val operatorText = operatorView.text.toString()
        val numberText = inputExpression.string

        val updatedText = when {
            operatorText.startsWith("[]") -> operatorText.replaceFirst("[]", numberText)
            operatorText.contains("[]") -> operatorText.replaceFirst("[]", numberText)
            else -> operatorText
        }

        operatorView.text = updatedText
        numberView.visibility = View.GONE // Hide the number view after using it
        Log.d(TAG, "Left expression: ${operator.leftExpression?.string}")
        Log.d(TAG, "Right expression: ${operator.rightExpression?.string}")
        // Check if the operator view is fully populated and update its tag with the new expression
        if (operator.leftExpression != null && operator.rightExpression != null) {
            val newExpression = Expression.BinaryExpression(operator.leftExpression!!, operator, operator.rightExpression!!)
            operatorView.tag = newExpression
            resultTextView.text = "${newExpression.value} (${newExpression.string})"
            Log.d(TAG, "Updated operator view: $updatedText")
            Log.d(TAG, "Expression created: ${newExpression.value} (${newExpression.string})")
            operator.leftExpression = null
            operator.rightExpression = null
        }
    }
}
