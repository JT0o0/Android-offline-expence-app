package com.toting.ledger.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs

/**
 * Formats/parses amounts stored as [Long] minor units (1/100 of the currency).
 * Whole amounts drop the ".00"; otherwise two decimals are shown.
 */
object MoneyFormatter {
    const val DEFAULT_SYMBOL = "NT$"

    /** Hard cap on a single amount: ±10^15 major units (10^17 minor), well within Long range. */
    const val MAX_AMOUNT_MINOR = 100_000_000_000_000_000L
    private val MAX_MINOR_BD = java.math.BigDecimal.valueOf(MAX_AMOUNT_MINOR)
    private val MIN_MINOR_BD = MAX_MINOR_BD.negate()

    private val grouping = DecimalFormat("#,##0")

    fun format(amountMinor: Long, symbol: String = DEFAULT_SYMBOL, showSymbol: Boolean = true): String {
        val negative = amountMinor < 0
        val absValue = abs(amountMinor)
        val whole = absValue / 100
        val frac = (absValue % 100).toInt()

        val sb = StringBuilder()
        if (negative) sb.append('-')
        if (showSymbol) sb.append(symbol)
        sb.append(grouping.format(whole))
        if (frac != 0) sb.append('.').append(frac.toString().padStart(2, '0'))
        return sb.toString()
    }

    /** Signed display, e.g. "+NT$120" / "-NT$80". */
    fun formatSigned(amountMinor: Long, positive: Boolean, symbol: String = DEFAULT_SYMBOL): String {
        val sign = if (positive) "+" else "-"
        return sign + format(abs(amountMinor), symbol)
    }

    /**
     * Converts a decimal major-unit value (e.g. from the calculator) to minor units,
     * clamped to ±[MAX_AMOUNT_MINOR] so huge inputs/results can never overflow Long.
     */
    fun toMinor(value: BigDecimal): Long =
        value.movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .max(MIN_MINOR_BD)
            .min(MAX_MINOR_BD)
            .toLong()

    fun toMajor(amountMinor: Long): BigDecimal =
        BigDecimal(amountMinor).movePointLeft(2)
}
