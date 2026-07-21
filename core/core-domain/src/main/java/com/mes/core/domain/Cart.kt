package com.mes.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class CartLine(
    val id: String,
    @SerialName("product") val productId: String,
    @SerialName("rental_start") val rentalStart: String,
    @SerialName("rental_end") val rentalEnd: String,
    val quantity: Int = 1,
    @SerialName("added_at") val addedAt: String,
    // Transient fields for UI, not from API
    @Transient val productName: String = "",
    @Transient val dailyRateTzs: Long = 0,
    @Transient val merchantName: String = "",
    @Transient val merchantId: String = "",
    @Transient val thumbnailUrl: String = ""
) {
    val numberOfDays: Int
        get() = try {
            val start = LocalDate.parse(rentalStart)
            val end = LocalDate.parse(rentalEnd)
            maxOf(1, end.toEpochDays() - start.toEpochDays())
        } catch (e: Exception) {
            1
        }

    val lineTotalTzs: Long
        get() = dailyRateTzs * quantity * numberOfDays
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
