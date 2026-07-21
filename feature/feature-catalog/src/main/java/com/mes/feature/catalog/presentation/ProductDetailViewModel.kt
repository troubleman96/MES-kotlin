package com.mes.feature.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.Product
import com.mes.core.network.CatalogApi
import com.mes.core.network.envelope.ApiResult
import com.mes.core.network.envelope.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mes.core.network.CartApi
import com.mes.core.network.CartSyncRequest
import com.mes.core.domain.CartLine
import kotlinx.datetime.Clock
import javax.inject.Inject

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val addToCartSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val catalogApi: CatalogApi,
    private val cartApi: CartApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall { catalogApi.getProduct(productId) }) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(product = result.data, isLoading = false) }
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

    fun addToCart(quantity: Int, rentalStart: String, rentalEnd: String) {
        val product = uiState.value.product ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Get current cart
            val currentCartResult = safeApiCall { cartApi.getCart() }
            val lines = if (currentCartResult is ApiResult.Success) {
                currentCartResult.data.lines.toMutableList()
            } else {
                mutableListOf()
            }

            // Check if already in cart
            val existingIndex = lines.indexOfFirst { 
                it.productId == product.id && it.rentalStart == rentalStart && it.rentalEnd == rentalEnd 
            }
            
            if (existingIndex != -1) {
                val existing = lines[existingIndex]
                lines[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
            } else {
                lines.add(
                    CartLine(
                        id = "", 
                        productId = product.id,
                        rentalStart = rentalStart,
                        rentalEnd = rentalEnd,
                        quantity = quantity,
                        addedAt = Clock.System.now().toString(),
                        productName = product.name,
                        dailyRateTzs = product.dailyRateTzs,
                        merchantName = product.merchantName,
                        merchantId = product.merchant ?: "",
                        thumbnailUrl = product.images.firstOrNull()?.url ?: ""
                    )
                )
            }

            // Sync cart
            when (val result = safeApiCall { cartApi.syncCart(CartSyncRequest(lines)) }) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, addToCartSuccess = true) }
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
    
    fun resetAddToCart() {
        _uiState.update { it.copy(addToCartSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
