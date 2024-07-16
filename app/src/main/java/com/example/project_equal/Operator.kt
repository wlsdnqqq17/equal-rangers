package com.example.project_equal

import kotlin.math.cbrt
import kotlin.math.sqrt

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
            if (right == 0.0) return 0.0
            return left / (right ?: 1.0)
        }
    }

    class Negation : Operator("--") {
        override fun apply(left: Double, right: Double?): Double {
            return -left
        }
    }

     class Sqrt : Operator("sqrt") {
         override fun apply(left: Double, right: Double?): Double {
             return sqrt(left)
         }
     }

    class Square : Operator("^") {
        override fun apply(left: Double, right: Double?): Double {
            return left * left
        }
    }

    class Cube : Operator("^^") {
        override fun apply(left: Double, right: Double?): Double {
            return left * left * left
        }
    }

    class Cbrt : Operator("cbrt") {
        override fun apply(left: Double, right: Double?): Double {
            return cbrt(left)
        }
    }

    class Colon : Operator(":") {
        override fun apply(left: Double, right: Double?): Double {
            return 10 * left + (right ?: 0.0)
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
        "--" -> Operator.Negation()
        "^" -> Operator.Square()
        "sqrt" -> Operator.Sqrt()
        "^^" -> Operator.Cube()
        "cbrt" -> Operator.Cbrt()
        else -> null
    }
}