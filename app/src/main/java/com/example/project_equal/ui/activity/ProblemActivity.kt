package com.example.project_equal.ui.activity

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.project_equal.Expression
import com.example.project_equal.Operator
import com.example.project_equal.R
import com.example.project_equal.getOperator

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
        createDraggableItemView(number.toString(), Expression.Number(number), R.drawable.number)
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
        val imgSrcId = when (operator) {
            is Operator.Addition -> R.drawable.plusbutton
            is Operator.Subtraction -> R.drawable.minusbutton
            is Operator.Multiplication -> R.drawable.multiplybutton
            is Operator.Division -> R.drawable.dividebutton
            is Operator.Negation -> R.drawable.minusbutton
            is Operator.Sqrt -> R.drawable.root2button
            is Operator.Square -> R.drawable.power2button
            is Operator.Cube -> R.drawable.power3button
            is Operator.Cbrt -> R.drawable.root3button
            is Operator.Equal -> R.drawable.equalbutton
            else -> R.drawable.plusbutton
        }
        createDraggableItemView(text, operator, imgSrcId)
    }

    private fun createDraggableItemView(text: String, tag: Any, imageResId: Int = R.drawable.plus) {

        val draggableItemLayout = RelativeLayout(this).apply {
            this.tag = tag
        }

        val imageView = ImageView(this).apply {
            setImageResource(imageResId)
            id = View.generateViewId()
        }

        val textView = TextView(this).apply {
            this.text = text
            id = View.generateViewId()
            // 텍스트 중앙 정렬
            this.gravity = Gravity.CENTER
        }

        val imageParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        }

        val textParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        }

        draggableItemLayout.addView(imageView, imageParams)
        draggableItemLayout.addView(textView, textParams)

        draggableItemLayout.setOnTouchListener { view, event ->
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

        addItemToLayout(draggableItemLayout)
    }

    private fun addItemToLayout(draggableItem: View) {
        val layout = findViewById<RelativeLayout>(R.id.root_layout)
        layout.post {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }
            layout.addView(draggableItem, params)
            Log.d("ProblemActivity", "Draggable item created")
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
            DragEvent.ACTION_DROP ->
                {
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
                    handleRegularDrop(draggedView, x, y, event)
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

    private fun handleRegularDrop(draggedView: View, x: Int, y: Int, event: DragEvent) {
        val layout = findViewById<RelativeLayout>(R.id.root_layout)  // Correct parent layout
        val overlappingView = findOverlappingView(x, y, draggedView, layout)
        if (overlappingView != null && overlappingView is RelativeLayout && isOperator(overlappingView.tag)) {
            updateOperatorView(overlappingView, draggedView as RelativeLayout)
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

    private fun isOperator(tag: Any): Boolean {
        return  tag is Operator
    }

    private fun updateOperatorView(operatorView: RelativeLayout, numberView: RelativeLayout) {
        val operator = operatorView.tag as? Operator ?: return
        val inputExpression = numberView.tag as? Expression ?: return
        Log.d(TAG, "inputExpression: $inputExpression")

        if (operator.leftExpression == null) {
            operator.leftExpression = inputExpression
        } else if (operator.rightExpression == null) {
            operator.rightExpression = inputExpression
        }

        val operatorTextView = operatorView.getChildAt(1) as? TextView
        val operatorText = operatorTextView!!.text.toString()
        val numberText = inputExpression.string

        val updatedText = operatorText.replaceFirst("[]", numberText)

        operatorTextView.text = updatedText
        numberView.visibility = View.GONE

        if (operator is Operator.Negation || operator is Operator.Sqrt || operator is Operator.Square || operator is Operator.Cube || operator is Operator.Cbrt) {
            handleUnaryOperator(operatorView, operator)
        } else if (operator.leftExpression != null && operator.rightExpression != null) {
            handleBinaryOperator(operatorView, operator)
        }
    }

    private fun handleUnaryOperator(operatorView: RelativeLayout, operator: Operator) {
        val newExpression = Expression.UnaryExpression(operator, operator.leftExpression!!)
        addScoreForOperator(operator.symbol)
        operatorView.tag = newExpression
        resultTextView.text = "$score"
        operator.leftExpression = null
        operator.rightExpression = null
    }

    private fun handleBinaryOperator(operatorView: RelativeLayout, operator: Operator) {
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

    private fun resetOperatorView(operator: Operator, operatorView: RelativeLayout) {
        createDraggableExpr(operator.leftExpression!!)
        createDraggableExpr(operator.rightExpression!!)
        operator.leftExpression = null
        operator.rightExpression = null
        val operatorTextView = operatorView.getChildAt(1) as? TextView
        operatorTextView!!.text = "[] ${operator.symbol} []"
        createDraggableItem(operator)
        (operatorView.parent as ViewGroup).removeView(operatorView)
    }

    private fun handleEqualExpression() {
        val resultIntent = Intent().apply { putExtra("SCORE", score) }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun finalizeOperatorView(operatorView: RelativeLayout, newExpression: Expression.BinaryExpression) {
        val operator = newExpression.operator // Extract the operator from newExpression
        addScoreForOperator(operator.symbol)
        operatorView.tag = newExpression
        resultTextView.text = "$score"
        resultTextView.append("\n${newExpression.value}, (${newExpression.string})") // Append the result instead of replacing the score
        operator.leftExpression = null
        operator.rightExpression = null
    }
}
