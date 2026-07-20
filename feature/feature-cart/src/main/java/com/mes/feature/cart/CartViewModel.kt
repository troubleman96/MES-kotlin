package com.mes.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.Cart
import com.mes.core.domain.CartLine
import com.mes.core.domain.RentalPeriod
import com.mes.core.testing.TestData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val cart: Cart = Cart(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        // Load demo cart for MVP
        loadDemoCart()
    }

    private fun loadDemoCart() {
        val demoLines = listOf(
            TestData.testCartLine(dailyRate = 50000),
            TestData.testCartLine(dailyRate = 30000),
            TestData.testCartLine(dailyRate = 75000)
        )
        _uiState.update { it.copy(cart = Cart(lines = demoLines)) }
    }

    fun removeLine(lineId: String) {
        viewModelScope.launch {
            val currentLines = _uiState.value.cart.lines
            _uiState.update {
                it.copy(cart = Cart(lines = currentLines.filter { line -> line.id != lineId }))
            }
        }
    }

    fun updateRentalPeriod(lineId: String, period: RentalPeriod) {
        viewModelScope.launch {
            val currentLines = _uiState.value.cart.lines.map { line ->
                if (line.id == lineId) line.copy(rentalPeriod = period) else line
            }
            _uiState.update { it.copy(cart = Cart(lines = currentLines)) }
        }
    }

    fun updateQuantity(lineId: String, quantity: Int) {
        viewModelScope.launch {
            val currentLines = _uiState.value.cart.lines.map { line ->
                if (line.id == lineId) line.copy(quantity = maxOf(1, quantity)) else line
            }
            _uiState.update { it.copy(cart = Cart(lines = currentLines)) }
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = Cart()) }
    }
}
