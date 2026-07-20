package com.mes.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class RentalPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    val numberOfDays: Int
        get() {
            val start = startDate.year * 365 + startDate.dayOfYear
            val end = endDate.year * 365 + endDate.dayOfYear
            return maxOf(1, end - start)
        }

    fun totalCost(dailyRate: Long): Long = dailyRate * numberOfDays

    fun isValid(): Boolean = endDate >= startDate
}

@Serializable
data class Money(
    val amountTzs: Long,
    val displayText: String = formatTzs(amountTzs)
) {
    companion object {
        fun formatTzs(amount: Long): String = "TZS %,d".format(amount)
    }
}
