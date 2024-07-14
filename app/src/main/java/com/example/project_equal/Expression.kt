package com.example.project_equal


sealed class Expression {
    abstract val value: Int
    abstract val string: String

    data class Number(val number: Int) : Expression() {
        override val value: Int
            get() = number

        override val string: String
            get() = number.toString()
    }

    data class BinaryExpression(val left: Expression, val operator: Operator, val right: Expression) : Expression() {
        override val value: Int
            get() = operator.apply(left.value, right.value)
        override val string: String
            get() = "${left.string} ${operator.symbol} ${right.string}"
    }

    data class UnaryExpression(val operator: Operator, val expr: Expression) : Expression() {
        override val value: Int
            get() = operator.apply(expr.value, null)

        override val string: String
            get() = "${operator.symbol}${expr.string}"
    }
}
