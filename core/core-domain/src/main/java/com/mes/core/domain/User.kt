package com.mes.core.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class UserRole {
    @SerialName("buyer") BUYER,
    @SerialName("merchant") MERCHANT
}

@Serializable
data class User(
    val id: String,
    val email: String,
    val phone: String? = null,
    @SerialName("phone_verified") val phoneVerified: Boolean = false,
    val role: UserRole,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("facility_name") val facilityName: String? = null,
    @SerialName("business_name") val businessName: String? = null,
    @SerialName("is_verified_merchant") val isVerifiedMerchant: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("product_count") val productCount: Int = 0
)
