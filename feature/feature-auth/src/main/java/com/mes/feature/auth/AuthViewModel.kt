package com.mes.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.datastore.SessionDataStore
import com.mes.core.domain.UserRole
import com.mes.core.network.AuthApi
import com.mes.core.network.LoginRequest
import com.mes.core.network.RegisterRequest
import com.mes.core.network.envelope.ApiResult
import com.mes.core.network.envelope.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistrationSuccess: Boolean = false,
    val requiresOtp: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(
        email: String,
        phone: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole,
        businessName: String? = null,
        businessRegNumber: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = safeApiCall {
                authApi.register(
                    RegisterRequest(
                        email = email,
                        phone = phone,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        role = role.name,
                        businessName = businessName,
                        businessRegistrationNumber = businessRegNumber
                    )
                )
            }) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistrationSuccess = true
                    )
                }
                is ApiResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Network error. Please check your connection."
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = safeApiCall {
                authApi.login(LoginRequest(email = email, password = password))
            }) {
                is ApiResult.Success -> {
                    val response = result.data
                    if (response.requiresOtpVerification) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            requiresOtp = true
                        )
                    } else {
                        sessionDataStore.saveSession(
                            accessToken = response.accessToken,
                            refreshToken = response.refreshToken,
                            userId = response.user.id,
                            role = response.user.role
                        )
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Network error. Please check your connection."
                    )
                }
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = safeApiCall {
                authApi.verifyOtp(
                    com.mes.core.network.OtpRequest(phone = phone, otp = otp)
                )
            }) {
                is ApiResult.Success -> {
                    val response = result.data
                    sessionDataStore.saveSession(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        userId = response.user.id,
                        role = response.user.role
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is ApiResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Network error. Please check your connection."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
