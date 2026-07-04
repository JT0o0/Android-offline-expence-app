package com.toting.ledger

import com.toting.ledger.util.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class MoneyFormatterTest {

    @Test fun wholeAmountDropsDecimals() {
        assertEquals("NT$123", MoneyFormatter.format(12_300))
    }

    @Test fun fractionalAmountShowsTwoDecimals() {
        assertEquals("NT$123.45", MoneyFormatter.format(12_345))
    }

    @Test fun negativeAmount() {
        assertEquals("-NT$80", MoneyFormatter.format(-8_000))
    }

    @Test fun grouping() {
        assertEquals("1,234,567", MoneyFormatter.format(123_456_700, showSymbol = false))
    }

    @Test fun toMinorWholeNumber() {
        assertEquals(12_300L, MoneyFormatter.toMinor(BigDecimal("123")))
    }

    @Test fun toMinorWithDecimals() {
        assertEquals(12_345L, MoneyFormatter.toMinor(BigDecimal("123.45")))
    }

    @Test fun toMajorRoundTrip() {
        assertEquals(0, MoneyFormatter.toMajor(12_345).compareTo(BigDecimal("123.45")))
    }
}
