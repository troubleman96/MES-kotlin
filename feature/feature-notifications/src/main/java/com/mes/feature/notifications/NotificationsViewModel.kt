package com.mes.feature.notifications

import androidx.lifecycle.ViewModel
import com.mes.core.domain.Notification
import com.mes.core.domain.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState

    init {
        loadDemoNotifications()
    }

    private fun loadDemoNotifications() {
        val now = Clock.System.now()
        val notifications = listOf(
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.ORDER_CONFIRMED,
                title = "Order Confirmed",
                body = "Your rental of Portable Ventilator Pro has been confirmed.",
                createdAt = now,
                relatedOrderId = "ORD-001"
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.DISPATCHED,
                title = "Order Dispatched",
                body = "Your order ORD-002 has been dispatched by MedTech Supplies.",
                createdAt = now,
                isRead = true,
                relatedOrderId = "ORD-002"
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.RETURN_DUE,
                title = "Return Due Soon",
                body = "Your rental of Autoclave Sterilizer is due for return in 2 days.",
                createdAt = now,
                relatedOrderId = "ORD-003"
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                type = NotificationType.PAYMENT_RECEIVED,
                title = "Payment Received",
                body = "Payment of TZS 350,000 received for order ORD-001.",
                createdAt = now,
                isRead = true
            )
        )
        _uiState.value = NotificationsUiState(
            notifications = notifications,
            unreadCount = notifications.count { !it.isRead }
        )
    }

    fun markAsRead(notificationId: String) {
        val updated = _uiState.value.notifications.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        _uiState.value = _uiState.value.copy(
            notifications = updated,
            unreadCount = updated.count { !it.isRead }
        )
    }

    fun markAllAsRead() {
        val updated = _uiState.value.notifications.map { it.copy(isRead = true) }
        _uiState.value = _uiState.value.copy(
            notifications = updated,
            unreadCount = 0
        )
    }
}
