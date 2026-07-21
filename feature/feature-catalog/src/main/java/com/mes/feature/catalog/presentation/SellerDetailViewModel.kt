package com.mes.feature.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.Product
import com.mes.core.domain.User
import com.mes.core.network.CatalogApi
import com.mes.core.network.envelope.ApiResult
import com.mes.core.network.envelope.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SellerDetailUiState(
    val merchant: User? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SellerDetailViewModel @Inject constructor(
    private val catalogApi: CatalogApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellerDetailUiState())
    val uiState: StateFlow<SellerDetailUiState> = _uiState.asStateFlow()

    fun loadSeller(sellerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val merchantResult = safeApiCall { catalogApi.getMerchant(sellerId) }

            when (merchantResult) {
                is ApiResult.Success -> {
                    val merchant = merchantResult.data
                    // Fetch all products and filter by this merchant's ID
                    val productsResult = safeApiCall {
                        catalogApi.getProducts(perPage = 100)
                    }
                    val merchantProducts = if (productsResult is ApiResult.Success) {
                        productsResult.data.items.filter { it.merchant == sellerId }
                    } else emptyList()

                    _uiState.update {
                        it.copy(
                            merchant = merchant,
                            products = merchantProducts,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = merchantResult.message) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update { it.copy(isLoading = false, error = "Network error") }
                }
            }
        }
    }
}
