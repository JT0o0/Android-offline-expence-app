package com.toting.ledger.util

import java.time.LocalDate
import java.time.YearMonth

/** Date helpers. java.time is available natively (minSdk 26). */
object DateUtils {

    private val weekdayZh = arrayOf("一", "二", "三", "四", "五", "六", "日")

    fun today(): LocalDate = LocalDate.now()

    fun fromEpochDay(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    /** Inclusive [first, last] epoch-day bounds of the given month. */
    fun monthRange(yearMonth: YearMonth): Pair<Long, Long> =
        yearMonth.atDay(1).toEpochDay() to yearMonth.atEndOfMonth().toEpochDay()

    /** e.g. "今天 6月28日 週六", "昨天 6月27日 週五", or "6月20日 週六". */
    fun formatDayHeader(date: LocalDate): String {
        val md = "${date.monthValue}月${date.dayOfMonth}日"
        val weekday = "週" + weekdayZh[date.dayOfWeek.value - 1]
        val prefix = when (date) {
            today() -> "今天 "
            today().minusDays(1) -> "昨天 "
            else -> ""
        }
        return "$prefix$md $weekday"
    }

    /** e.g. "2026年6月". */
    fun formatMonth(yearMonth: YearMonth): String =
        "${yearMonth.year}年${yearMonth.monthValue}月"
}
