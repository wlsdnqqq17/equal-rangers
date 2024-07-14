// ExpressionTest.kt
package com.example.project_equal

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionTest {

    @Test
    //3
    fun testNumberExpression() {
        val numberExpr = Expression.Number(3)
        assertEquals(3, numberExpr.value)
    }

    @Test
    //(4+5)
    fun testAdditionExpression() {
        val leftExpr = Expression.Number(4)
        val rightExpr = Expression.Number(5)
        val additionExpr = Expression.BinaryExpression(leftExpr, Operator.Addition, rightExpr)
        assertEquals(9, additionExpr.value)
    }

    @Test
    //(4+5)*3
    fun testMultiplicationExpression() {
        val leftExpr = Expression.Number(4)
        val rightExpr = Expression.Number(5)
        val additionExpr = Expression.BinaryExpression(leftExpr, Operator.Addition, rightExpr)
        val multiplyExpr = Expression.BinaryExpression(additionExpr, Operator.Multiplication, Expression.Number(3))
        assertEquals(27, multiplyExpr.value)
    }

    @Test
    //sqrt(9)
    fun testSquareRootExpression() {
        val sqrtExpr = Expression.UnaryExpression(Operator.SquareRoot, Expression.Number(9))
        assertEquals(3, sqrtExpr.value)
    }

    @Test
    //square(5)
    fun testSquareExpression() {
        val squareExpr = Expression.UnaryExpression(Operator.Square, Expression.Number(5))
        assertEquals(25, squareExpr.value)
    }

    @Test
    //(2+3)*(7-4)
    fun testComplexExpression() {
        val complexLeftExpr = Expression.BinaryExpression(Expression.Number(2), Operator.Addition, Expression.Number(3))
        val complexRightExpr = Expression.BinaryExpression(Expression.Number(7), Operator.Subtraction, Expression.Number(4))
        val complexExpr = Expression.BinaryExpression(complexLeftExpr, Operator.Multiplication, complexRightExpr)
        assertEquals(15, complexExpr.value)
    }

}


