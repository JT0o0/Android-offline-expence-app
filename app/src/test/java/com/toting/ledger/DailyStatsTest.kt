package com.toting.ledger

import com.toting.ledger.data.local.TransactionEntity
import com.toting.ledger.data.model.TxType
import com.toting.ledger.ui.stats.buildDailyStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DailyStatsTest {

    private fun tx(type: TxType, amountMinor: Long, date: LocalDate) = TransactionEntity(
        type = type,
        amountMinor = amountMinor,
        accountId = 1L,
        dateEpochDay = date.toEpochDay(),
    )

    private val ym: YearMonth = YearMonth.of(2026, 7)
    private fun day(d: Int): LocalDate = ym.atDay(d)

    @Test
    fun emptyMonthIsDenseAndAllZero() {
        val dailies = buildDailyStats(ym, emptyList())
        assertEquals(31, dailies.size)
        assertEquals((1..31).toList(), dailies.map { it.day })
        assertTrue(dailies.all { it.income == 0L && it.expense == 0L && it.cumulative == 0L })
    }

    @Test
    fun februaryLength() {
        assertEquals(28, buildDailyStats(YearMonth.of(2026, 2), emptyList()).size)
        assertEquals(29, buildDailyStats(YearMonth.of(2028, 2), emptyList()).size)
    }

    @Test
    fun cumulativeFoldsInDayOrder() {
        val dailies = buildDailyStats(
            ym,
            listOf(
                tx(TxType.INCOME, 10_000, day(1)),
                tx(TxType.EXPENSE, 3_000, day(2)),
                tx(TxType.EXPENSE, 2_000, day(5)),
            ),
        )
        assertEquals(10_000L, dailies[0].cumulative)
        assertEquals(7_000L, dailies[1].cumulative)
        assertEquals(7_000L, dailies[2].cumulative) // no tx on day 3 → carries over
        assertEquals(5_000L, dailies[4].cumulative)
        assertEquals(5_000L, dailies[30].cumulative)
    }

    @Test
    fun cumulativeGoesNegativeThenRecovers() {
        val dailies = buildDailyStats(
            ym,
            listOf(
                tx(TxType.EXPENSE, 5_000, day(1)),
                tx(TxType.INCOME, 8_000, day(10)),
            ),
        )
        assertEquals(-5_000L, dailies[0].cumulative)
        assertEquals(-5_000L, dailies[8].cumulative)
        assertEquals(3_000L, dailies[9].cumulative)
    }

    @Test
    fun multipleTransactionsOnOneDayAggregate() {
        val dailies = buildDailyStats(
            ym,
            listOf(
                tx(TxType.EXPENSE, 1_000, day(3)),
                tx(TxType.EXPENSE, 2_500, day(3)),
                tx(TxType.INCOME, 4_000, day(3)),
            ),
        )
        assertEquals(3_500L, dailies[2].expense)
        assertEquals(4_000L, dailies[2].income)
        assertEquals(500L, dailies[2].cumulative)
    }
}
