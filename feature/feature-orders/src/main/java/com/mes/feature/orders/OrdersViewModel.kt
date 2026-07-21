package com.mes.feature.orders

import androidx.lifecycle.ViewModel
import com.mes.core.domain.Order
import com.mes.core.domain.OrderLine
import com.mes.core.domain.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.datetime.*
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
                id = "SO-001",
                orderGroupId = "OG-001",
                merchantName = "MedTech Supplies Ltd",
                status = OrderStatus.DELIVERED,
                subtotalTzs = 350000,
                createdAt = Clock.System.now().toString(),
                lines = listOf(
                    OrderLine(
                        id = "L1",
                        productName = "Portable Ventilator Pro",
                        dailyRateTzs = 50000,
                        rentalStart = today.minus(3, DateTimeUnit.DAY).toString(),
                        rentalEnd = today.plus(4, DateTimeUnit.DAY).toString(),
                        quantity = 1,
                        lineTotalTzs = 350000
                    )
                )
            ),
            Order(
                id = "SO-002",
                orderGroupId = "OG-002",
                merchantName = "HealthCare Equipment Co",
                status = OrderStatus.DISPATCHED,
                subtotalTzs = 500000,
                createdAt = Clock.System.now().toString(),
                lines = listOf(
                    OrderLine(
                        id = "L2",
                        productName = "Patient Monitor 12-Lead",
                        dailyRateTzs = 25000,
                        rentalStart = today.plus(2, DateTimeUnit.DAY).toString(),
                        rentalEnd = today.plus(12, DateTimeUnit.DAY).toString(),
                        quantity = 2,
                        lineTotalTzs = 500000
                    )
                )
            ),
            Order(
                id = "SO-003",
                orderGroupId = "OG-003",
                merchantName = "SterilPro Solutions",
                status = OrderStatus.RETURNED,
                subtotalTzs = 105000,
                createdAt = Clock.System.now().toString(),
                lines = listOf(
                    OrderLine(
                        id = "L3",
                        productName = "Autoclave Sterilizer 23L",
                        dailyRateTzs = 15000,
                        rentalStart = today.minus(10, DateTimeUnit.DAY).toString(),
                        rentalEnd = today.minus(3, DateTimeUnit.DAY).toString(),
                        quantity = 1,
                        lineTotalTzs = 105000
                    )
                )
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
