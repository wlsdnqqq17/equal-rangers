package com.example.project_equal

 abstract class Operator(val symbol: String) {
    var leftExpression: Expression? = null
    var rightExpression: Expression? = null

    abstract fun apply(left: Double, right: Double?): Double

    class Equal : Operator("=") {
        override fun apply(left: Double, right: Double?): Double {
            return if (left == right) 1.0 else 0.0
        }
    }

    class Addition : Operator("+") {
        override fun apply(left: Double, right: Double?): Double {
            return left + (right ?: 0.0)
        }
    }

    class Subtraction : Operator("-") {
        override fun apply(left: Double, right: Double?): Double {
            return left - (right ?: 0.0)
        }
    }

    class Multiplication : Operator("*") {
        override fun apply(left: Double, right: Double?): Double {
            return left * (right ?: 1.0)
        }
    }

    class Division : Operator("/") {
        override fun apply(left: Double, right: Double?): Double {
            if (right == 0.0) throw ArithmeticException("Division by zero")
            return left / (right ?: 1.0)
        }
    }

    class Negation : Operator("-") {
        override fun apply(left: Double, right: Double?): Double {
            return -left
        }
    }
}

fun getOperator(symbol: String): Operator? {
    return when (symbol) {
        "=" -> Operator.Equal()
        "+" -> Operator.Addition()
        "-" -> Operator.Subtraction()
        "*" -> Operator.Multiplication()
        "/" -> Operator.Division()
        else -> null
    }
}