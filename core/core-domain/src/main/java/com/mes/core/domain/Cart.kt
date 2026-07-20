package com.mes.core.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CartLine(
    val id: String,
    val productId: String,
    val merchantId: String,
    val merchantName: String,
    val productName: String,
    val thumbnailUrl: String,
    val dailyRateTzs: Long,
    val rentalPeriod: RentalPeriod,
    val quantity: Int = 1,
    val addedAt: Instant
) {
    val lineTotalTzs: Long
        get() = rentalPeriod.totalCost(dailyRateTzs) * quantity

    val merchantSubtotalTzs: Long
        get() = lineTotalTzs
}

@Serializable
data class Cart(
    val lines: List<CartLine> = emptyList()
) {
    val groupedByMerchant: Map<String, List<CartLine>>
        get() = lines.groupBy { it.merchantId }

    val grandTotalTzs: Long
        get() = lines.sumOf { it.lineTotalTzs }

    val isEmpty: Boolean
        get() = lines.isEmpty()

    val isNotEmpty: Boolean
        get() = lines.isNotEmpty()

    val itemCount: Int
        get() = lines.size
}
