package com.toting.ledger

import com.toting.ledger.util.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class ExpressionEvaluatorTest {

    private fun eval(expr: String) = ExpressionEvaluator.evaluate(expr)

    private fun assertValue(expected: String, expr: String) {
        val result = eval(expr)
        assertEquals("expr=$expr", 0, result!!.compareTo(BigDecimal(expected)))
    }

    @Test fun addition() = assertValue("155", "120+35")
    @Test fun subtraction() = assertValue("60", "100-40")
    @Test fun multiplication() = assertValue("42", "6×7")
    @Test fun division() = assertValue("2.5", "10÷4")

    @Test fun precedence() = assertValue("14", "2+3×4")

    @Test fun decimals() = assertValue("13", "12.5+0.5")

    @Test fun trailingOperatorIsIgnored() = assertValue("100", "100+")

    @Test fun leadingZeroDecimal() = assertValue("0.5", "0.5")

    @Test fun emptyIsNull() = assertNull(eval(""))

    @Test fun divisionByZeroIsNull() = assertNull(eval("5÷0"))

    @Test fun invalidIsNull() = assertNull(eval("abc"))
}
