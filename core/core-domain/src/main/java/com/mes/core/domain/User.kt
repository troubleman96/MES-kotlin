package com.mes.core.domain

import kotlinx.serialization.Serializable

enum class UserRole {
    BUYER,
    MERCHANT
}

@Serializable
data class User(
    val id: String,
    val email: String,
    val phone: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isPhoneVerified: Boolean = false,
    val isEmailVerified: Boolean = false,
    val preferredLanguage: String = "en",
    val businessName: String? = null,
    val businessRegistrationNumber: String? = null
)
