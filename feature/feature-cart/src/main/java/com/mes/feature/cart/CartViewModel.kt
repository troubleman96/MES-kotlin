package com.mes.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.Cart
import com.mes.core.domain.RentalPeriod
import com.mes.core.testing.TestData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.mes.core.network.CartApi
import com.mes.core.network.CartSyncRequest
import com.mes.core.network.envelope.ApiResult
import com.mes.core.network.envelope.safeApiCall

data class CartUiState(
    val cart: Cart = Cart(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartApi: CartApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall { cartApi.getCart() }) {
                is ApiResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            cart = Cart(lines = result.data.lines),
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update { it.copy(isLoading = false, error = "Network error") }
                }
            }
        }
    }

    fun removeLine(lineId: String) {
        viewModelScope.launch {
            val currentLines = _uiState.value.cart.lines.filter { it.id != lineId }
            syncCart(currentLines)
        }
    }

    fun updateRentalPeriod(lineId: String, period: RentalPeriod) {
        viewModelScope.launch {
            val currentLines = _uiState.value.cart.lines.map { line ->
                if (line.id == lineId) line.copy(
                    rentalStart = period.startDate.toString(),
                    rentalEnd = period.endDate.toString()
                ) else line
            }
            syncCart(currentLines)
        }
    }

    fun updateQuantity(lineId: String, quantity: Int) {
        viewModelScope.launch {
            val currentLines = _uiState.value.cart.lines.map { line ->
                if (line.id == lineId) line.copy(quantity = maxOf(1, quantity)) else line
            }
            syncCart(currentLines)
        }
    }

    private suspend fun syncCart(lines: List<com.mes.core.domain.CartLine>) {
        _uiState.update { it.copy(isLoading = true) }
        when (val result = safeApiCall { cartApi.syncCart(CartSyncRequest(lines = lines)) }) {
            is ApiResult.Success -> {
                _uiState.update { 
                    it.copy(
                        cart = Cart(lines = result.data.cart.lines),
                        isLoading = false
                    )
                }
            }
            is ApiResult.Failure -> {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
            is ApiResult.NetworkError -> {
                _uiState.update { it.copy(isLoading = false, error = "Network error") }
            }
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = Cart()) }
    }
}
