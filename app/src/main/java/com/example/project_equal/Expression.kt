package com.example.project_equal


sealed class Expression {
    abstract val value: Double
    abstract val string: String
    abstract val symbol: String

    data class Number(val number: Int) : Expression() {
        override val value: Double
            get() = number.toDouble()

        override val string: String
            get() = number.toString()

        override val symbol = ""
    }

    data class BinaryExpression(var left: Expression, val operator: Operator, var right: Expression) : Expression() {
        override val value: Double
            get() = operator.apply(left.value, right.value)
        override val string: String
            get() = "${left.string} ${operator.symbol} ${right.string}"
        override val symbol = operator.symbol
    }

    data class UnaryExpression(val operator: Operator, var expr: Expression) : Expression() {
        override val value: Double
            get() = operator.apply(expr.value, null)

        override val string: String
            get() = "${operator.symbol}${expr.string}"

        override val symbol = operator.symbol
    }
}
