package com.example.project_equal

sealed class Operator(val symbol: String) {
    var leftExpression: Expression? = null
    var rightExpression: Expression? = null

    abstract fun apply(left: Int, right: Int?): Int

    class Addition : Operator("+") {
        override fun apply(left: Int, right: Int?): Int {
            return left + (right ?: 0)
        }
    }

    class Subtraction : Operator("-") {
        override fun apply(left: Int, right: Int?): Int {
            return left - (right ?: 0)
        }
    }

    class Multiplication : Operator("*") {
        override fun apply(left: Int, right: Int?): Int {
            return left * (right ?: 1)
        }
    }

    class Division : Operator("/") {
        override fun apply(left: Int, right: Int?): Int {
            if (right == 0) throw ArithmeticException("Division by zero")
            return left / (right ?: 1)
        }
    }

    class Negation : Operator("-") {
        override fun apply(left: Int, right: Int?): Int {
            return -left
        }
    }
}
