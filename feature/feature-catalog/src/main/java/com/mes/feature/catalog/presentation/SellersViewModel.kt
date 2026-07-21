package com.mes.feature.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.User
import com.mes.core.network.AuthApi // We might need a generic user/merchant list API
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

data class SellersUiState(
    val sellers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SellersViewModel @Inject constructor(
    private val catalogApi: CatalogApi // Assuming CatalogApi has getMerchants or similar
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellersUiState())
    val uiState: StateFlow<SellersUiState> = _uiState.asStateFlow()

    init {
        loadSellers()
    }

    fun loadSellers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // For now, we'll try to get sellers from a likely endpoint or filter products
            // If there's no getMerchants, we can derive from products for now
            // But let's assume the API has a way to get merchants.
            // If getMerchants doesn't exist, we'll have to adjust CatalogApi.
            
            when (val result = safeApiCall { catalogApi.getProducts() }) {
                is ApiResult.Success -> {
                    // Extract unique merchants from products as a fallback
                    // In a real app, you'd have @GET("merchants")
                    val items = result.data.items
                    // This is a dummy way to show "real" data from the products returned by API
                    // We'll create "User" objects from product merchant info for UI consistency
                    val sellers = items.map { 
                        User(
                            id = it.merchant ?: "unknown",
                            firstName = it.merchantName.split(" ").firstOrNull() ?: "Merchant",
                            lastName = it.merchantName.split(" ").lastOrNull() ?: "",
                            role = com.mes.core.domain.UserRole.MERCHANT,
                            email = "",
                            businessName = it.merchantName,
                            isVerifiedMerchant = it.merchantIsVerified
                        )
                    }.distinctBy { it.id }

                    _uiState.update { it.copy(sellers = sellers, isLoading = false) }
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
}
