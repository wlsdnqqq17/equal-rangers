package com.example.project_equal

sealed class Operator(val symbol: String) {
    var leftExpression: Expression? = null
    var rightExpression: Expression? = null

    abstract fun apply(left: Int, right: Int?): Int

    object Addition : Operator("+") {
        override fun apply(left: Int, right: Int?): Int {
            return left + (right ?: 0)
        }
    }

    object Subtraction : Operator("-") {
        override fun apply(left: Int, right: Int?): Int {
            return left - (right ?: 0)
        }
    }

    object Multiplication : Operator("*") {
        override fun apply(left: Int, right: Int?): Int {
            return left * (right ?: 1)
        }
    }

    object Division : Operator("/") {
        override fun apply(left: Int, right: Int?): Int {
            if (right == 0) throw ArithmeticException("Division by zero")
            return left / (right ?: 1)
        }
    }

}