package com.example.project_equal


// Expression.kt
sealed class Expression {
    abstract val value: Int

    data class Number(val number: Int) : Expression() {
        override val value: Int
            get() = number
    }

    data class BinaryExpression(val left: Expression, val operator: Operator, val right: Expression) : Expression() {
        override val value: Int
            get() = operator.apply(left.value, right.value)
    }

    data class UnaryExpression(val operator: Operator, val expr: Expression) : Expression() {
        override val value: Int
            get() = operator.apply(expr.value, null)
    }
}
