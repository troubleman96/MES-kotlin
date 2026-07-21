package com.mes.core.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    @SerialName("order_group") val orderGroupId: String? = null,
    val merchant: String? = null,
    @SerialName("merchant_name") val merchantName: String? = null,
    val status: OrderStatus,
    @SerialName("special_instructions") val specialInstructions: String? = null,
    @SerialName("subtotal_tzs") val subtotalTzs: Long,
    @SerialName("created_at") val createdAt: String,
    val lines: List<OrderLine> = emptyList(),
    @SerialName("delivery_address") val deliveryAddress: Address? = null,
    @SerialName("billing_address") val billingAddress: Address? = null
)

@Serializable
enum class OrderStatus {
    @SerialName("pending_payment") PENDING_PAYMENT,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("dispatched") DISPATCHED,
    @SerialName("delivered") DELIVERED,
    @SerialName("return_due") RETURN_DUE,
    @SerialName("returned") RETURNED,
    @SerialName("cancelled") CANCELLED,
    @SerialName("payment_failed") PAYMENT_FAILED
}

@Serializable
data class OrderLine(
    val id: String,
    val product: String? = null, // Product ID
    @SerialName("product_name_snapshot") val productName: String,
    @SerialName("daily_rate_snapshot_tzs") val dailyRateTzs: Long,
    @SerialName("rental_start") val rentalStart: String,
    @SerialName("rental_end") val rentalEnd: String,
    val quantity: Int,
    @SerialName("line_total_tzs") val lineTotalTzs: Long
)
