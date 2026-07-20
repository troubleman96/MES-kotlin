package com.mes.feature.orders

import androidx.lifecycle.ViewModel
import com.mes.core.domain.FulfillmentStatus
import com.mes.core.domain.Order
import com.mes.core.domain.OrderItem
import com.mes.core.domain.OrderStatus
import com.mes.core.domain.PaymentStatus
import com.mes.core.domain.RentalPeriod
import com.mes.core.domain.SubOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import java.util.UUID
import javax.inject.Inject

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class OrdersViewModel @Inject constructor() : ViewModel() {

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(OrdersUiState())
    val uiState = _uiState

    init {
        loadDemoOrders()
    }

    private fun loadDemoOrders() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val demoOrders = listOf(
            Order(
                id = "ORD-001",
                orderGroupId = "OG-001",
                subOrders = listOf(
                    SubOrder(
                        id = "SO-001",
                        orderId = "ORD-001",
                        merchantId = "merchant-1",
                        merchantName = "MedTech Supplies Ltd",
                        items = listOf(
                            OrderItem(
                                productId = "prod-1",
                                productName = "Portable Ventilator Pro",
                                thumbnailUrl = "https://via.placeholder.com/100",
                                dailyRateTzs = 50000,
                                quantity = 1,
                                rentalPeriod = RentalPeriod(
                                    startDate = today.minus(3, DateTimeUnit.DAY),
                                    endDate = today.plus(4, DateTimeUnit.DAY)
                                )
                            )
                        ),
                        totalTzs = 350000,
                        paymentStatus = PaymentStatus.COMPLETED,
                        fulfillmentStatus = FulfillmentStatus.DELIVERED,
                        createdAt = Clock.System.now()
                    )
                ),
                createdAt = Clock.System.now(),
                status = OrderStatus.DELIVERED
            ),
            Order(
                id = "ORD-002",
                orderGroupId = "OG-002",
                subOrders = listOf(
                    SubOrder(
                        id = "SO-002",
                        orderId = "ORD-002",
                        merchantId = "merchant-2",
                        merchantName = "HealthCare Equipment Co",
                        items = listOf(
                            OrderItem(
                                productId = "prod-2",
                                productName = "Patient Monitor 12-Lead",
                                thumbnailUrl = "https://via.placeholder.com/100",
                                dailyRateTzs = 25000,
                                quantity = 2,
                                rentalPeriod = RentalPeriod(
                                    startDate = today.plus(2, DateTimeUnit.DAY),
                                    endDate = today.plus(12, DateTimeUnit.DAY)
                                )
                            )
                        ),
                        totalTzs = 500000,
                        paymentStatus = PaymentStatus.COMPLETED,
                        fulfillmentStatus = FulfillmentStatus.DISPATCHED,
                        createdAt = Clock.System.now()
                    )
                ),
                createdAt = Clock.System.now(),
                status = OrderStatus.DISPATCHED
            ),
            Order(
                id = "ORD-003",
                orderGroupId = "OG-003",
                subOrders = listOf(
                    SubOrder(
                        id = "SO-003",
                        orderId = "ORD-003",
                        merchantId = "merchant-3",
                        merchantName = "SterilPro Solutions",
                        items = listOf(
                            OrderItem(
                                productId = "prod-3",
                                productName = "Autoclave Sterilizer 23L",
                                thumbnailUrl = "https://via.placeholder.com/100",
                                dailyRateTzs = 15000,
                                quantity = 1,
                                rentalPeriod = RentalPeriod(
                                    startDate = today.minus(10, DateTimeUnit.DAY),
                                    endDate = today.minus(3, DateTimeUnit.DAY)
                                )
                            )
                        ),
                        totalTzs = 105000,
                        paymentStatus = PaymentStatus.COMPLETED,
                        fulfillmentStatus = FulfillmentStatus.RETURNED,
                        createdAt = Clock.System.now()
                    )
                ),
                createdAt = Clock.System.now(),
                status = OrderStatus.RETURNED
            )
        )
        _uiState.value = OrdersUiState(orders = demoOrders)
    }

    fun selectOrder(orderId: String) {
        _uiState.value = _uiState.value.copy(
            selectedOrder = _uiState.value.orders.find { it.id == orderId }
        )
    }
}
