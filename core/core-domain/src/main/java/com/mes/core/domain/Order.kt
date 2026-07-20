package com.mes.core.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val orderGroupId: String,
    val subOrders: List<SubOrder>,
    val createdAt: Instant,
    val status: OrderStatus
)

@Serializable
enum class OrderStatus(val displayName: String) {
    PENDING_PAYMENT("Pending Payment"),
    CONFIRMED("Confirmed"),
    DISPATCHED("Dispatched"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered"),
    IN_USE("In Use"),
    RETURN_DUE("Return Due"),
    RETURNED("Returned"),
    CANCELLED("Cancelled"),
    PAYMENT_FAILED("Payment Failed")
}

@Serializable
data class SubOrder(
    val id: String,
    val orderId: String,
    val merchantId: String,
    val merchantName: String,
    val items: List<OrderItem>,
    val totalTzs: Long,
    val paymentStatus: PaymentStatus,
    val fulfillmentStatus: FulfillmentStatus,
    val deliveryAddress: Address? = null,
    val contractUrl: String? = null,
    val createdAt: Instant,
    val rentalPeriod: RentalPeriod? = null
)

@Serializable
enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    EXPIRED("Expired"),
    REFUNDED("Refunded")
}

@Serializable
enum class FulfillmentStatus(val displayName: String) {
    PENDING("Pending"),
    DISPATCHED("Dispatched"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered"),
    RETURNED("Returned")
}

@Serializable
data class OrderItem(
    val productId: String,
    val productName: String,
    val thumbnailUrl: String,
    val dailyRateTzs: Long,
    val quantity: Int,
    val rentalPeriod: RentalPeriod
) {
    val lineTotalTzs: Long
        get() = rentalPeriod.totalCost(dailyRateTzs) * quantity
}
