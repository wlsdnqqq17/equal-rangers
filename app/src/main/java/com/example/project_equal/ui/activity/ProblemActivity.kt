package com.example.project_equal.ui.activity

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.Expression
import com.example.project_equal.Operator
import com.example.project_equal.R
import com.example.project_equal.getOperator
import com.google.android.material.button.MaterialButton

class ProblemActivity : AppCompatActivity() {

    private val TAG = "ProblemActivity"
    private lateinit var deleteButton: ImageButton
    private lateinit var disasamButton: ImageButton
    private lateinit var resultTextView: TextView
    private lateinit var problemNumber: String
    private var score: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problemm)

        initializeViews()
        setupListeners()

        problemNumber = intent.getStringExtra("PROBLEM_NUMBER")!!
        findViewById<TextView>(R.id.problem_text).text = problemNumber

        createDraggableNumbersFromProblem(problemNumber)
    }

    private fun initializeViews() {
        resultTextView = findViewById(R.id.result_text)
        deleteButton = findViewById(R.id.delete_button)
        disasamButton = findViewById(R.id.disassemble_button)
    }

    private fun setupListeners() {
        val operators = mapOf(
            R.id.plus_button to Operator.Addition(),
            R.id.minus_button to Operator.Subtraction(),
            R.id.multiply_button to Operator.Multiplication(),
            R.id.divide_button to Operator.Division(),
            R.id.negation_button to Operator.Negation(),
            R.id.sqrt_button to Operator.Sqrt(),
            R.id.square_button to Operator.Square(),
            R.id.cube_button to Operator.Cube(),
            R.id.cbrt_button to Operator.Cbrt()
        )

        for ((buttonId, operator) in operators) {
            findViewById<ImageButton>(buttonId).setOnClickListener { createDraggableItem(operator) }
        }

        findViewById<ImageButton>(R.id.equal_button).setOnClickListener { createDraggableEqual(Operator.Equal()) }
        findViewById<RelativeLayout>(R.id.root_layout).setOnDragListener(dragListener)
    }

    private fun createDraggableNumbersFromProblem(problem: String) {
        problem.forEach { char ->
            if (char.isDigit()) {
                createDraggableNumber(char - '0')
            }
        }
    }

    private fun createDraggableExpr(expr: Expression) {
        createDraggableItemView(expr.string, expr)
    }

    private fun createDraggableEqual(operator: Operator) {
        createDraggableItemView("[] = []", operator)
    }

    private fun createDraggableNumber(number: Int) {
        createDraggableItemView(number.toString(), Expression.Number(number))
    }

    private fun createDraggableItem(operator: Operator) {
        if (operator is Operator.Equal) {
            createDraggableItemView("[] = []", operator)
            return
        }

        val text = when (operator) {
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
        createDraggableItemView(text, operator)
    }

    private fun createDraggableItemView(text: String, tag: Any) {
        // RelativeLayout을 생성하여 Button을 포함
        val draggableItemLayout = RelativeLayout(this)

        // Button 생성
        val draggableItem = Button(this).apply {
            this.text = text
            this.tag = tag
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

        // LayoutParams 설정
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT)
        }

        // RelativeLayout에 Button 추가
        draggableItemLayout.addView(draggableItem, params)

        // 부모 레이아웃에 draggableItemLayout 추가
        addItemToLayout(draggableItemLayout)
    }

    private fun addItemToLayout(draggableItem: View) {
        val layout = findViewById<RelativeLayout>(R.id.root_layout)
        layout.post {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            layout.addView(draggableItem, params)
            Log.d("ProblemActivity", "Draggable item created")
        }
    }



    private fun findEmptySpaceForNewItem(layout: RelativeLayout, newItem: View): Pair<Int, Int> {
        val occupiedSpaces = mutableListOf<Rect>()
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            if (child.visibility == View.VISIBLE) {
                val location = IntArray(2)
                child.getLocationOnScreen(location)
                val rect = Rect(location[0], location[1], location[0] + child.width, location[1] + child.height)
                occupiedSpaces.add(rect)
            }
        }

        val newItemWidth = 200 // newItem.layoutParams.width
        val newItemHeight = 200 // newItem.layoutParams.height

        val screenWidth = layout.width
        val screenHeight = layout.height

        for (y in 0 until screenHeight step newItemHeight + 16) {
            for (x in 0 until screenWidth step newItemWidth + 16) {
                val newItemRect = Rect(x, y, x + newItemWidth, y + newItemHeight)
                if (occupiedSpaces.none { Rect.intersects(it, newItemRect) }) {
                    return Pair(x, y)
                }
            }
        }

        return Pair(0, 0)
    }



    private fun addItemToLayout(draggableItem: TextView) {
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
        score += when (operator) {
            "+", "-" -> 1
            "*", "/" -> 2
            "--" -> 1
            "sqrt", "^", "^^", "cbrt" -> 3
            else -> 0
        }
        Log.d(TAG, "Score updated: $score")
    }

    private fun subtractScoreForOperator(operator: String) {
        score -= when (operator) {
            "+", "-" -> 1
            "*", "/" -> 2
            "--" -> 1
            "sqrt", "^", "^^", "cbrt" -> 3
            else -> 0
        }
        Log.d(TAG, "Score updated: $score")
    }

    private val dragListener = View.OnDragListener { v, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            DragEvent.ACTION_DRAG_ENTERED -> true
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED -> true
            DragEvent.ACTION_DROP -> handleDropEvent(event)
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

    private fun handleDropEvent(event: DragEvent): Boolean {
        val draggedView = event.localState as View
        val owner = draggedView.parent as ViewGroup
        owner.removeView(draggedView)

        val x = event.x.toInt()
        val y = event.y.toInt()

        if (isDropOnDeleteButton(x, y)) {
            handleDeleteButtonDrop(draggedView)
        } else if (isDropOnDisassembleButton(x, y)) {
            handleDisassembleButtonDrop(draggedView)
        } else {
            handleRegularDrop(draggedView, x, y, event)
        }
        return true
    }

    private fun isDropOnDeleteButton(x: Int, y: Int): Boolean {
        return isDropOnButton(x, y, deleteButton)
    }

    private fun isDropOnDisassembleButton(x: Int, y: Int): Boolean {
        return isDropOnButton(x, y, disasamButton)
    }

    private fun isDropOnButton(x: Int, y: Int, button: ImageButton): Boolean {
        val location = IntArray(2)
        button.getLocationOnScreen(location)
        val buttonX = location[0]
        val buttonY = location[1]
        val buttonWidth = button.width
        val buttonHeight = button.height

        return x in buttonX..(buttonX + buttonWidth) && y in buttonY..(buttonY + buttonHeight)
    }

    private fun handleDeleteButtonDrop(draggedView: View) {
        val draggedViewTag = draggedView.tag
        if (draggedViewTag is Operator && draggedViewTag.leftExpression == null) {
            // Handle the delete button drop
        }
    }

    private fun handleDisassembleButtonDrop(draggedView: View) {
        val draggedViewTag = draggedView.tag

        if (draggedViewTag is Operator && draggedViewTag.leftExpression != null) {
            createDraggableExpr(draggedViewTag.leftExpression!!)
            draggedViewTag.leftExpression = null
            createDraggableItem(draggedViewTag)
        } else if (draggedViewTag is Expression && draggedViewTag.string.length != 1) {
            handleExpressionDisassembly(draggedViewTag)
        }
    }

    private fun handleExpressionDisassembly(expr: Expression) {
        if (expr is Expression.BinaryExpression) {
            createDraggableExpr(expr.left)
            createDraggableExpr(expr.right)
        } else if (expr is Expression.UnaryExpression) {
            createDraggableExpr(expr.expr)
        }
        subtractScoreForOperator(expr.symbol)
        resultTextView.text = "$score"
        createDraggableItem(getOperator(expr.symbol)!!)
    }

    private fun handleRegularDrop(draggedView: View, x: Int, y: Int, event: DragEvent) {
        val layout = findViewById<RelativeLayout>(R.id.root_layout)  // Correct parent layout
        val overlappingView = findOverlappingView(x, y, draggedView, layout)

        if (overlappingView != null && overlappingView is TextView && isOperator(overlappingView)) {
            updateOperatorView(overlappingView, draggedView as TextView)
        } else {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.leftMargin = x - (draggedView.width / 2)
            params.topMargin = y - (draggedView.height / 2)
            layout.addView(draggedView, params)  // Add to correct parent layout
            draggedView.visibility = View.VISIBLE
            Log.d(TAG, "View dropped at x: $x, y: $y")
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
                if (x in childX..(childX + child.width) && y in childY..(childY + child.height)) {
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

        val updatedText = operatorText.replaceFirst("[]", numberText)

        operatorView.text = updatedText
        numberView.visibility = View.GONE

        if (operator is Operator.Negation || operator is Operator.Sqrt || operator is Operator.Square || operator is Operator.Cube || operator is Operator.Cbrt) {
            handleUnaryOperator(operatorView, operator)
        } else if (operator.leftExpression != null && operator.rightExpression != null) {
            handleBinaryOperator(operatorView, operator)
        }
    }

    private fun handleUnaryOperator(operatorView: TextView, operator: Operator) {
        val newExpression = Expression.UnaryExpression(operator, operator.leftExpression!!)
        addScoreForOperator(operator.symbol)
        operatorView.tag = newExpression
        resultTextView.text = "$score"
        Log.d(TAG, "Updated operator view: ${operatorView.text}")
        Log.d(TAG, "Expression created: ${newExpression.value} (${newExpression.string})")
        operator.leftExpression = null
        operator.rightExpression = null
    }

    private fun handleBinaryOperator(operatorView: TextView, operator: Operator) {
        val newExpression = Expression.BinaryExpression(operator.leftExpression!!, operator, operator.rightExpression!!)
        val filteredText = newExpression.string.filterNot { it in "+-*/= sqrtcbrt^^".toList() }
        val isContained = problemNumber.contains(filteredText)

        if (operator.symbol == "/" && operator.rightExpression!!.value == 0.0) {
            resetOperatorView(operator, operatorView)
        } else if (!isContained) {
            resetOperatorView(operator, operatorView)
        } else if (newExpression.value == 1.0 && newExpression.operator.symbol == "=") {
            handleEqualExpression()
        } else {
            finalizeOperatorView(operatorView, newExpression)
        }
    }

    private fun resetOperatorView(operator: Operator, operatorView: TextView) {
        createDraggableExpr(operator.leftExpression!!)
        createDraggableExpr(operator.rightExpression!!)
        operator.leftExpression = null
        operator.rightExpression = null
        operatorView.text = "[] ${operator.symbol} []"
        createDraggableItem(operator)
        (operatorView.parent as ViewGroup).removeView(operatorView)
    }

    private fun handleEqualExpression() {
        val resultIntent = Intent().apply { putExtra("SCORE", score) }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun finalizeOperatorView(operatorView: TextView, newExpression: Expression.BinaryExpression) {
        val operator = newExpression.operator // Extract the operator from newExpression
        addScoreForOperator(operator.symbol)
        operatorView.tag = newExpression
        resultTextView.text = "$score"
        resultTextView.append("\n${newExpression.value}, (${newExpression.string})") // Append the result instead of replacing the score
        Log.d(TAG, "Updated operator view: ${operatorView.text}")
        Log.d(TAG, "Expression created: ${newExpression.value} (${newExpression.string})")
        operator.leftExpression = null
        operator.rightExpression = null
    }

}
