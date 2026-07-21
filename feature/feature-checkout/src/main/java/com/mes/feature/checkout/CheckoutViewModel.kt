package com.mes.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.Address
import com.mes.core.domain.Cart
import com.mes.core.network.AddressApi
import com.mes.core.network.CartApi
import com.mes.core.network.OrdersApi
import com.mes.core.network.CheckoutRequest
import com.mes.core.network.envelope.ApiResult
import com.mes.core.network.envelope.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mes.core.network.AuthApi
import javax.inject.Inject

enum class CheckoutStep { REVIEW, ADDRESS, PAYMENT, CONFIRMED }

data class CheckoutUiState(
    val currentStep: CheckoutStep = CheckoutStep.REVIEW,
    val cart: Cart = Cart(),
    val addresses: List<Address> = emptyList(),
    val stepProgress: Float = 0.25f,
    val selectedAddressId: String? = null,
    val paymentPhone: String = "",
    val paymentNetwork: String = "M-Pesa",
    val isProcessingPayment: Boolean = false,
    val paymentComplete: Boolean = false,
    val orderConfirmed: Boolean = false,
    val orderGroupId: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val ordersApi: OrdersApi,
    private val cartApi: CartApi,
    private val addressApi: AddressApi,
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val cartResult = safeApiCall { cartApi.getCart() }
            val addressResult = safeApiCall { addressApi.getAddresses() }
            val profileResult = safeApiCall { authApi.getProfile() }

            if (cartResult is ApiResult.Success) {
                _uiState.update { it.copy(cart = Cart(lines = cartResult.data.lines)) }
            }
            if (addressResult is ApiResult.Success) {
                val addresses = addressResult.data
                _uiState.update { 
                    it.copy(
                        addresses = addresses,
                        selectedAddressId = addresses.find { a -> a.isDefault }?.id ?: addresses.firstOrNull()?.id
                    )
                }
            }
            if (profileResult is ApiResult.Success) {
                _uiState.update { it.copy(paymentPhone = profileResult.data.phone ?: "") }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun nextStep() {
        val steps = CheckoutStep.entries
        val currentIndex = steps.indexOf(_uiState.value.currentStep)
        
        if (_uiState.value.currentStep == CheckoutStep.ADDRESS && _uiState.value.selectedAddressId == null) {
            _uiState.update { it.copy(error = "Please select a delivery address") }
            return
        }

        if (currentIndex < steps.size - 1) {
            val nextStep = steps[currentIndex + 1]
            _uiState.update {
                it.copy(
                    currentStep = nextStep,
                    stepProgress = (currentIndex + 2).toFloat() / steps.size,
                    error = null
                )
            }
        }
    }

    fun previousStep() {
        val steps = CheckoutStep.entries
        val currentIndex = steps.indexOf(_uiState.value.currentStep)
        if (currentIndex > 0) {
            val prevStep = steps[currentIndex - 1]
            _uiState.update {
                it.copy(
                    currentStep = prevStep,
                    stepProgress = currentIndex.toFloat() / steps.size,
                    error = null
                )
            }
        }
    }

    fun selectAddress(addressId: String) {
        _uiState.update { it.copy(selectedAddressId = addressId) }
    }

    fun updatePaymentPhone(phone: String) {
        _uiState.update { it.copy(paymentPhone = phone) }
    }

    fun processPayment() {
        val addressId = _uiState.value.selectedAddressId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true, error = null) }
            
            // 1. Create order group via checkout
            val checkoutResult = safeApiCall { 
                ordersApi.checkout(
                    CheckoutRequest(
                        deliveryAddressId = addressId,
                        billingAddressId = addressId
                    )
                )
            }

            if (checkoutResult is ApiResult.Success) {
                val orderGroupId = checkoutResult.data.orderGroupId
                val subOrders = checkoutResult.data.subOrders
                
                // 2. For each sub-order, initiate payment (Snippe push)
                // In a simplified MVP we might just initiate for the first one or a bundled one if API supports
                // Assuming we initiate payment for the group or the first sub-order for now
                val firstSubOrder = subOrders.firstOrNull()
                if (firstSubOrder != null) {
                    val payResult = safeApiCall { ordersApi.initiatePayment(firstSubOrder.id) }
                    
                    if (payResult is ApiResult.Success) {
                        // Poll for payment status
                        var status = "pending"
                        repeat(10) { // Poll for ~30 seconds
                            delay(3000)
                            val statusResult = safeApiCall { ordersApi.getPaymentStatus(firstSubOrder.id) }
                            if (statusResult is ApiResult.Success) {
                                status = statusResult.data.status
                                if (status == "completed" || status == "paid") {
                                    _uiState.update { 
                                        it.copy(
                                            isProcessingPayment = false,
                                            paymentComplete = true,
                                            orderConfirmed = true,
                                            orderGroupId = orderGroupId
                                        )
                                    }
                                    return@launch
                                } else if (status == "failed") {
                                    _uiState.update { 
                                        it.copy(
                                            isProcessingPayment = false, 
                                            error = "Payment failed. Please try again." 
                                        )
                                    }
                                    return@launch
                                }
                            }
                        }
                        // If we timed out polling, but it might still succeed
                        _uiState.update { it.copy(orderConfirmed = true, isProcessingPayment = false) }
                    } else if (payResult is ApiResult.Failure) {
                        _uiState.update { it.copy(isProcessingPayment = false, error = payResult.message) }
                    }
                }
            } else if (checkoutResult is ApiResult.Failure) {
                _uiState.update { it.copy(isProcessingPayment = false, error = checkoutResult.message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
