// Operator.kt
package com.example.project_equal

sealed class Operator {
    abstract fun apply(left: Int?, right: Int?): Int

    object Addition : Operator() {
        override fun apply(left: Int?, right: Int?): Int {
            return (left ?: 0) + (right ?: 0)
        }
    }

    object Subtraction : Operator() {
        override fun apply(left: Int?, right: Int?): Int {
            return (left ?: 0) - (right ?: 0)
        }
    }

    object Multiplication : Operator() {
        override fun apply(left: Int?, right: Int?): Int {
            return (left ?: 0) * (right ?: 0)
        }
    }

    object Division : Operator() {
        override fun apply(left: Int?, right: Int?): Int {
            if (right == 0) throw ArithmeticException("Division by zero")
            return (left ?: 0) / (right ?: 1)
        }
    }

    object SquareRoot : Operator() {
        override fun apply(left: Int?, right: Int?): Int {
            return kotlin.math.sqrt((left ?: 0).toDouble()).toInt()
        }
    }

    object Square : Operator() {
        override fun apply(left: Int?, right: Int?): Int {
            return (left ?: 0) * (left ?: 0)
        }
    }
}
