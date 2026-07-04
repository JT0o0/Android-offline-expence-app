package com.toting.ledger.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Evaluates simple arithmetic expressions for the in-app calculator.
 * Supports + - × ÷ (and their ASCII forms) with operator precedence and decimals.
 * A trailing operator is ignored so live typing ("120+") still produces a result.
 * Returns null for empty/invalid expressions or division by zero.
 */
object ExpressionEvaluator {

    fun evaluate(expression: String): BigDecimal? {
        var s = expression
            .replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace(" ", "")
        // Allow a leading sign by treating it as 0 ± ...
        if (s.startsWith("+") || s.startsWith("-")) s = "0$s"
        // Drop a dangling trailing operator while typing.
        while (s.isNotEmpty() && isOp(s.last())) s = s.dropLast(1)
        if (s.isEmpty()) return null

        val tokens = tokenize(s) ?: return null
        val rpn = toRpn(tokens) ?: return null
        return evalRpn(rpn)
    }

    private fun isOp(c: Char) = c == '+' || c == '-' || c == '*' || c == '/'

    private fun precedence(c: Char) = if (c == '*' || c == '/') 2 else 1

    private sealed interface Token {
        data class Num(val value: BigDecimal) : Token
        data class Op(val op: Char) : Token
    }

    private fun tokenize(s: String): List<Token>? {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectNumber = true
        while (i < s.length) {
            val c = s[i]
            when {
                c.isDigit() || c == '.' -> {
                    val start = i
                    var dots = 0
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                        if (s[i] == '.') dots++
                        i++
                    }
                    if (dots > 1) return null
                    val numStr = s.substring(start, i)
                    val value = numStr.toBigDecimalOrNull() ?: return null
                    tokens += Token.Num(value)
                    expectNumber = false
                }
                isOp(c) -> {
                    if (expectNumber) return null // two operators in a row
                    tokens += Token.Op(c)
                    expectNumber = true
                    i++
                }
                else -> return null
            }
        }
        if (expectNumber) return null // ended on an operator (shouldn't happen after trimming)
        return tokens
    }

    private fun toRpn(tokens: List<Token>): List<Token>? {
        val output = mutableListOf<Token>()
        val ops = ArrayDeque<Char>()
        for (t in tokens) {
            when (t) {
                is Token.Num -> output += t
                is Token.Op -> {
                    while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(t.op)) {
                        output += Token.Op(ops.removeLast())
                    }
                    ops.addLast(t.op)
                }
            }
        }
        while (ops.isNotEmpty()) output += Token.Op(ops.removeLast())
        return output
    }

    private fun evalRpn(rpn: List<Token>): BigDecimal? {
        val stack = ArrayDeque<BigDecimal>()
        for (t in rpn) {
            when (t) {
                is Token.Num -> stack.addLast(t.value)
                is Token.Op -> {
                    if (stack.size < 2) return null
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    val r = when (t.op) {
                        '+' -> a + b
                        '-' -> a - b
                        '*' -> a * b
                        '/' -> {
                            if (b.signum() == 0) return null
                            a.divide(b, 12, RoundingMode.HALF_UP).stripTrailingZeros()
                        }
                        else -> return null
                    }
                    stack.addLast(r)
                }
            }
        }
        return stack.singleOrNull()
    }
}
