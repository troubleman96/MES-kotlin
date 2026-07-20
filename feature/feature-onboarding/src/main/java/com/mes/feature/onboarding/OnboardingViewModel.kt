package com.mes.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mes.core.datastore.SessionDataStore
import com.mes.core.domain.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val selectedLanguage: String = "en",
    val currentStep: Int = 0
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setLanguage(language: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
        viewModelScope.launch {
            sessionDataStore.setLanguage(language)
        }
    }

    fun setCurrentStep(step: Int) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }

    fun markOnboardingComplete(role: UserRole) {
        viewModelScope.launch {
            sessionDataStore.setOnboardingComplete(role)
        }
    }
}
