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
            
            // In a real app we might have a specific endpoint for merchant products
            // For now we use the general search/filter if specific merchant-products isn't public
            val merchantResult = safeApiCall { catalogApi.getMerchant(sellerId) }
            val productsResult = safeApiCall { 
                catalogApi.getProducts(category = null, search = null) // We would ideally filter by merchant ID here
            }

            if (merchantResult is ApiResult.Success) {
                _uiState.update { 
                    it.copy(
                        merchant = merchantResult.data,
                        products = if (productsResult is ApiResult.Success) {
                            productsResult.data.items.filter { p -> p.merchant == sellerId || p.merchantName == merchantResult.data.businessName }
                        } else emptyList(),
                        isLoading = false
                    )
                }
            } else if (merchantResult is ApiResult.Failure) {
                _uiState.update { it.copy(isLoading = false, error = merchantResult.message) }
            }
        }
    }
}
