package com.mes.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.domain.Cart
import com.mes.core.domain.Order
import com.mes.core.testing.TestData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CheckoutStep { REVIEW, ADDRESS, PAYMENT, CONFIRMED }

data class CheckoutUiState(
    val currentStep: CheckoutStep = CheckoutStep.REVIEW,
    val cart: Cart = Cart(),
    val stepProgress: Float = 0.25f,
    val selectedAddressId: String? = null,
    val paymentPhone: String = "+255 712 345 678",
    val paymentNetwork: String = "M-Pesa",
    val isProcessingPayment: Boolean = false,
    val paymentComplete: Boolean = false,
    val orderConfirmed: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(cart = TestData.testCart()) }
    }

    fun nextStep() {
        val steps = CheckoutStep.entries
        val currentIndex = steps.indexOf(_uiState.value.currentStep)
        if (currentIndex < steps.size - 1) {
            val nextStep = steps[currentIndex + 1]
            _uiState.update {
                it.copy(
                    currentStep = nextStep,
                    stepProgress = (currentIndex + 2).toFloat() / steps.size
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
                    stepProgress = currentIndex.toFloat() / steps.size
                )
            }
        }
    }

    fun selectAddress(addressId: String) {
        _uiState.update { it.copy(selectedAddressId = addressId) }
    }

    fun processPayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true) }
            delay(3000) // Simulate USSD push + PIN entry
            _uiState.update {
                it.copy(
                    isProcessingPayment = false,
                    paymentComplete = true,
                    orderConfirmed = true
                )
            }
        }
    }
}
