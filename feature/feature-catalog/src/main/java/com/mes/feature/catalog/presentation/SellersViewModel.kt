package com.mes.feature.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.User
import com.mes.core.network.CatalogApi
import com.mes.core.network.envelope.ApiResult
import com.mes.core.network.envelope.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SellersUiState(
    val sellers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class SellersViewModel @Inject constructor(
    private val catalogApi: CatalogApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellersUiState())
    val uiState: StateFlow<SellersUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSellers()
    }

    fun loadSellers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val query = _uiState.value.searchQuery.ifBlank { null }

            when (val result = safeApiCall { catalogApi.getMerchants(search = query) }) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(sellers = result.data.items, isLoading = false)
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Network error. Check your connection.")
                    }
                }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            loadSellers()
        }
    }

    fun refresh() {
        loadSellers()
    }
}
