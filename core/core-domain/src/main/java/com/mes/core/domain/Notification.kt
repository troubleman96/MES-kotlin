package com.mes.core.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val isRead: Boolean = false,
    val relatedOrderId: String? = null
)

@Serializable
enum class NotificationType {
    ORDER_CONFIRMED,
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    RETURN_DUE,
    RETURN_OVERDUE,
    DISPATCHED,
    DELIVERED,
    MERCHANT_MESSAGE
}
