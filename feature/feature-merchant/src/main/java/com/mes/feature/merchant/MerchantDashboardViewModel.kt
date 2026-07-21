package com.mes.feature.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.Order
import com.mes.core.domain.Product
import com.mes.core.network.CatalogApi
import com.mes.core.network.OrdersApi
import com.mes.core.network.envelope.ApiResult
import com.mes.core.network.envelope.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MerchantDashboardUiState(
    val activeRentals: Int = 0,
    val monthlyRevenue: Long = 0,
    val incomingOrders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MerchantDashboardViewModel @Inject constructor(
    private val catalogApi: CatalogApi,
    private val ordersApi: OrdersApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(MerchantDashboardUiState())
    val uiState: StateFlow<MerchantDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val ordersResult = safeApiCall { ordersApi.getOrders() }
            
            when (ordersResult) {
                is ApiResult.Success -> {
                    val orders = ordersResult.data
                    val pendingOrders = orders.filter { it.status.lowercase() == "pending" || it.status.lowercase() == "confirmed" }
                    val activeRentals = orders.count { it.status.lowercase() == "active" || it.status.lowercase() == "delivered" }
                    val revenue = orders.filter { it.status.lowercase() != "cancelled" }.sumOf { it.totalAmountTzs }

                    _uiState.update { 
                        it.copy(
                            incomingOrders = pendingOrders.take(5),
                            activeRentals = activeRentals,
                            monthlyRevenue = revenue,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = ordersResult.message) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update { it.copy(isLoading = false, error = "Network error") }
                }
            }
        }
    }
}
