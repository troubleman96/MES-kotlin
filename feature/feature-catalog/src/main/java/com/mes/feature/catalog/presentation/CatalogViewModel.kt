package com.mes.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.Product
import com.mes.core.domain.ProductCategory
import com.mes.core.domain.RentalPeriod
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

data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedCategory: ProductCategory? = null,
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val selectedProduct: Product? = null,
    val selectedRentalPeriod: RentalPeriod? = null
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val catalogApi: CatalogApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadFeaturedProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall {
                catalogApi.getProducts(
                    page = _uiState.value.currentPage,
                    category = _uiState.value.selectedCategory?.name,
                    search = _uiState.value.searchQuery.ifBlank { null }
                )
            }) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            products = result.data.items,
                            isLoading = false,
                            hasMorePages = result.data.page < result.data.totalPages
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Network error")
                    }
                }
            }
        }
    }

    fun loadFeaturedProducts() {
        viewModelScope.launch {
            when (val result = safeApiCall { catalogApi.getFeaturedProducts() }) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(featuredProducts = result.data) }
                }
                else -> { /* silent fail for featured */ }
            }
        }
    }

    fun selectCategory(category: ProductCategory?) {
        _uiState.update { it.copy(selectedCategory = category, currentPage = 1) }
        loadProducts()
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query, currentPage = 1) }
        loadProducts()
    }

    fun loadMore() {
        if (_uiState.value.hasMorePages && !_uiState.value.isLoading) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            loadProducts()
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, currentPage = 1) }
        loadProducts()
        loadFeaturedProducts()
        _uiState.update { it.copy(isRefreshing = false) }
    }

    fun selectProduct(product: Product) {
        _uiState.update { it.copy(selectedProduct = product) }
    }

    fun setRentalPeriod(period: RentalPeriod) {
        _uiState.update { it.copy(selectedRentalPeriod = period) }
    }
}
