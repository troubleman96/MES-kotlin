package com.mes.feature.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.network.CatalogApi
import com.mes.core.network.CreateProductRequest
import com.mes.core.network.envelope.ApiResult
import com.mes.core.network.envelope.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddListingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddListingViewModel @Inject constructor(
    private val catalogApi: CatalogApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddListingUiState())
    val uiState: StateFlow<AddListingUiState> = _uiState.asStateFlow()

    fun addListing(
        name: String,
        category: String,
        priceTzs: Long,
        stock: Int,
        description: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val request = CreateProductRequest(
                name = name,
                description = description,
                dailyRateTzs = priceTzs,
                category = category.lowercase(),
                stock = stock
            )
            
            when (val result = safeApiCall { catalogApi.createProduct(request) }) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
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
    
    fun resetState() {
        _uiState.update { it.copy(isSuccess = false, error = null) }
    }
}
