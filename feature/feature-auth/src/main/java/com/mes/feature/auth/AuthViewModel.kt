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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistrationSuccess: Boolean = false,
    val requiresOtp: Boolean = false,
    val isLoggedIn: Boolean = false,
    val phone: String = ""
)

sealed interface AuthEvent {
    data object LoginSuccess : AuthEvent
    data object RegistrationSuccess : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionDataStore.accessToken.collectLatest { token ->
                // Don't set isLoggedIn=true if we are still waiting for OTP verification
                if (!_uiState.value.requiresOtp) {
                    _uiState.update { it.copy(isLoggedIn = !token.isNullOrBlank()) }
                }
            }
        }
    }

    fun register(
        email: String,
        phone: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole,
        businessName: String? = null,
        facilityName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall {
                authApi.register(
                    RegisterRequest(
                        email = email,
                        phone = phone,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        role = role.name.lowercase(),
                        businessName = businessName,
                        facilityName = facilityName
                    )
                )
            }) {
                is ApiResult.Success -> {
                    val response = result.data
                    // SAVE SESSION IMMEDIATELY so verifyPhone can use it if required by backend
                    val role = if (response.role?.lowercase() == "merchant") UserRole.MERCHANT else UserRole.BUYER
                    sessionDataStore.saveSession(
                        accessToken = response.accessToken ?: "",
                        refreshToken = response.refreshToken ?: "",
                        userId = response.userId ?: "",
                        role = role
                    )

                    _uiState.update { it.copy(
                        isLoading = false, 
                        isRegistrationSuccess = true,
                        requiresOtp = true,
                        phone = phone
                    ) }
                    _events.send(AuthEvent.RegistrationSuccess)
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Network error. Please check your connection.")
                    }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall {
                authApi.login(LoginRequest(email = email, password = password))
            }) {
                is ApiResult.Success -> {
                    val response = result.data
                    if (response.phoneVerified != true) {
                        _uiState.update { it.copy(isLoading = false, requiresOtp = true) }
                    } else {
                        val role = if (response.role?.lowercase() == "merchant") UserRole.MERCHANT else UserRole.BUYER
                        sessionDataStore.saveSession(
                            accessToken = response.accessToken ?: "",
                            refreshToken = response.refreshToken ?: "",
                            userId = response.userId ?: "",
                            role = role
                        )
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                        _events.send(AuthEvent.LoginSuccess)
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Network error. Please check your connection.")
                    }
                }
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall {
                authApi.verifyPhone(
                    com.mes.core.network.OtpRequest(phone = phone, otp = otp)
                )
            }) {
                is ApiResult.Success -> {
                    val response = result.data
                    val role = if (response.role?.lowercase() == "merchant") UserRole.MERCHANT else UserRole.BUYER
                    sessionDataStore.saveSession(
                        accessToken = response.accessToken ?: "",
                        refreshToken = response.refreshToken ?: "",
                        userId = response.userId ?: "",
                        role = role
                    )
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    _events.send(AuthEvent.LoginSuccess)
                }
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Network error. Please check your connection.")
                    }
                }
            }
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall { authApi.sendPhoneOtp() }) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    // Maybe show a success message?
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
