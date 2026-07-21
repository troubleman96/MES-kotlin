package com.mes.feature.merchant

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
import javax.inject.Inject

data class ManageListingsUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ManageListingsViewModel @Inject constructor(
    private val catalogApi: CatalogApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageListingsUiState())
    val uiState: StateFlow<ManageListingsUiState> = _uiState.asStateFlow()

    init {
        loadMyProducts()
    }

    fun loadMyProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Using getMyProducts endpoint from CatalogApi
            when (val result = safeApiCall { catalogApi.getMyProducts() }) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(products = result.data.items, isLoading = false) }
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
