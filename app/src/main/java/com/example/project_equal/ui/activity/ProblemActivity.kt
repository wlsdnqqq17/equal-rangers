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
import com.example.project_equal.Expression
import com.example.project_equal.Operator
import com.example.project_equal.R
import com.example.project_equal.getOperator

class ProblemActivity : AppCompatActivity() {

    private val TAG = "ProblemActivity"
    private lateinit var deleteButton: Button
    private lateinit var disasamButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var problemNumber: String
    private var score: Int = 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem)

        resultTextView = findViewById(R.id.result_text)
        deleteButton = findViewById(R.id.delete_button)
        disasamButton = findViewById(R.id.disassemble_button)
        val problemText: TextView = findViewById(R.id.problem_text)
        val plusButton: Button = findViewById(R.id.plus_button)
        val minusButton: Button = findViewById(R.id.minus_button)
        val multiplyButton: Button = findViewById(R.id.multiply_button)
        val divideButton: Button = findViewById(R.id.divide_button)
        val equalButton: Button = findViewById(R.id.equal_button)
        val negationButton: Button = findViewById(R.id.negation_button)
        val sqrtButton: Button = findViewById(R.id.sqrt_button)
        val squareButton: Button = findViewById(R.id.square_button)
        val cubeButton: Button = findViewById(R.id.cube_button)
        val cbrtButton: Button = findViewById(R.id.cbrt_button)

        val next_btn: Button = findViewById(R.id.next_button)


        plusButton.setOnClickListener { createDraggableItem(Operator.Addition()) }
        minusButton.setOnClickListener { createDraggableItem(Operator.Subtraction()) }
        multiplyButton.setOnClickListener { createDraggableItem(Operator.Multiplication()) }
        divideButton.setOnClickListener { createDraggableItem(Operator.Division()) }
        negationButton.setOnClickListener { createDraggableItem(Operator.Negation()) }
        sqrtButton.setOnClickListener { createDraggableItem(Operator.Sqrt()) }
        squareButton.setOnClickListener { createDraggableItem(Operator.Square()) }
        cubeButton.setOnClickListener { createDraggableItem(Operator.Cube()) }
        cbrtButton.setOnClickListener { createDraggableItem(Operator.Cbrt()) }


        equalButton.setOnClickListener { createDraggableEqual(Operator.Equal())}

        val layout = findViewById<RelativeLayout>(R.id.root_layout)
        layout.setOnDragListener(dragListener)
        problemNumber = intent.getStringExtra("PROBLEM_NUMBER")!!
        problemText.text = problemNumber

        problemNumber?.forEach { char ->
            if (char.isDigit()) {
                createDraggableNumber(char - '0')
            }
        }
        next_btn.setOnClickListener(){
            val intent = Intent(this, GameResult::class.java)
            intent.putExtra("PROBLEM_RESULT", 10)
            startActivity(intent)
        }
    }

    private fun createDraggableExpr(expr: Expression) {
        val draggableItem = TextView(this).apply {
            this.text = expr.string
            this.tag = expr
            textSize = 20f
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.color.holo_blue_light)
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val clipText = this.text
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
            Log.d(TAG, "Draggable string created: ${draggableItem.text}")
        }
    }

    private fun createDraggableEqual(operator: Operator) {
        val draggableItem = TextView(this).apply {
            text = "[] = []"
            tag = operator
            textSize = 20f
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.color.holo_red_dark)
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
                is Operator.Negation -> "-[]"
                is Operator.Sqrt -> "sqrt[]"
                is Operator.Square -> "[]^"
                is Operator.Cube -> "[]^^"
                is Operator.Cbrt -> "cbrt[]"
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


    private fun addScoreForOperator(operator: String) {
        when (operator) {
            "+", "-" -> score += 1
            "*", "/" -> score += 2
            "--" -> score += 1
            "sqrt" -> score += 3
            "^" -> score += 3
            "^^" -> score += 3
            "cbrt" -> score += 3
        }
        Log.d(TAG, "Score updated: $score")
    }

    private fun subtractScoreForOperator(operator: String) {
        when (operator) {
            "+", "-" -> score -= 1
            "*", "/" -> score -= 2
            "--" -> score -= 1
            "sqrt" -> score -= 3
            "^" -> score -= 3
            "^^" -> score -= 3
            "cbrt" -> score -= 3
        }
        Log.d(TAG, "Score updated: $score")
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
                val draggedViewTag = draggedView.tag


                val disasamButtonLocation = IntArray(2)
                disasamButton.getLocationOnScreen(disasamButtonLocation)
                val disasamButtonX = disasamButtonLocation[0]
                val disasamButtonY = disasamButtonLocation[1]
                val disasamButtonWidth = disasamButton.width
                val disasamButtonHeight = disasamButton.height

                if (x >= disasamButtonX && x <= disasamButtonX + disasamButtonWidth &&
                    y >= disasamButtonY && y <= disasamButtonY + disasamButtonHeight && draggedViewTag is Operator && draggedViewTag.leftExpression != null) {

                    createDraggableExpr(draggedViewTag.leftExpression!!)
                    draggedViewTag.leftExpression = null
                    createDraggableItem(draggedViewTag)


                }
                else if ( x >= disasamButtonX && x <= disasamButtonX + disasamButtonWidth &&
                    y >= disasamButtonY && y <= disasamButtonY + disasamButtonHeight && draggedViewTag is Expression && draggedViewTag.string.length != 1 ) {

                    if (draggedViewTag is Expression.BinaryExpression) {
                        createDraggableExpr(draggedViewTag.left)
                        createDraggableExpr(draggedViewTag.right)
                    } else if (draggedViewTag is Expression.UnaryExpression) {
                        createDraggableExpr(draggedViewTag.expr)
                    }
                    subtractScoreForOperator(draggedViewTag.symbol)
                    resultTextView.text = "${score}"
                    createDraggableItem(getOperator(draggedViewTag.symbol)!!)

                }


                else if (x >= deleteButtonX && x <= deleteButtonX + deleteButtonWidth &&
                    y >= deleteButtonY && y <= deleteButtonY + deleteButtonHeight && draggedViewTag is Operator && draggedViewTag.leftExpression == null) {
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
        return view.tag is Operator
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
        numberView.visibility = View.GONE
        if (operator is Operator.Negation || operator is Operator.Sqrt || operator is Operator.Square || operator is Operator.Cube || operator is Operator.Cbrt) {
            val newExpression = Expression.UnaryExpression(operator, operator.leftExpression!!)
            addScoreForOperator(operator.symbol)
            operatorView.tag = newExpression
            resultTextView.text = "${score}"
            Log.d(TAG, "Updated operator view: $updatedText")
            Log.d(TAG, "Expression created: ${newExpression.value} (${newExpression.string})")
            operator.leftExpression = null
            operator.rightExpression = null
        }
        else if (operator.leftExpression != null && operator.rightExpression != null ) {
            val newExpression = Expression.BinaryExpression(operator.leftExpression!!, operator, operator.rightExpression!!)

            val symbols = listOf('+', '-', '*', '/', '=', ' ', 's', 'q', 'r', 't', 'c', 'b', '^')
            val filteredText = newExpression.string.filter { it !in symbols }
            Log.d(TAG, "Filtered text: $filteredText")
            val isContained = problemNumber!!.contains(filteredText)

            if (operator.symbol == "/" && operator.rightExpression!!.value == 0.0) {
                createDraggableExpr(operator.leftExpression!!)
                createDraggableExpr(operator.rightExpression!!)
                operator.leftExpression = null
                operator.rightExpression = null
                operatorView.text = "[] " + operator.symbol + " []"
                createDraggableItem(operator)
                val layout = operatorView.parent as ViewGroup
                layout.removeView(operatorView)
            }

            else if (!isContained) {
                createDraggableExpr(operator.leftExpression!!)
                createDraggableExpr(operator.rightExpression!!)
                operator.leftExpression = null
                operator.rightExpression = null
                operatorView.text = "[] " + operator.symbol + " []"
                Log.d(TAG, "여기는 !isContained ${operator.symbol}")
                createDraggableItem(operator)
                val layout = operatorView.parent as ViewGroup
                layout.removeView(operatorView)
            }

            else if (newExpression.value == 1.0 && newExpression.operator.symbol == "=") {
                val resultIntent = Intent()
                resultIntent.putExtra("SCORE", score) // 점수값을 여기에 넣습니다.
                setResult(RESULT_OK, resultIntent)
                finish()
            }

            else {
                addScoreForOperator(operator.symbol)
                operatorView.tag = newExpression
                resultTextView.text = "${score}"
                resultTextView.text = "${newExpression.value}, (${newExpression.string})"
                Log.d(TAG, "Updated operator view: $updatedText")
                Log.d(TAG, "Expression created: ${newExpression.value} (${newExpression.string})")
                operator.leftExpression = null
                operator.rightExpression = null
            }
        }
    }
}
