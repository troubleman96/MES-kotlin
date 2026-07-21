package com.mes.core.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val description: String,
    val specs: Map<String, String> = emptyMap(),
    @SerialName("daily_rate_tzs") val dailyRateTzs: Long,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    val images: List<ProductImage> = emptyList(),
    val merchant: String? = null, // Merchant ID
    // Extra fields to match UI expectations if not directly in model but returned in list
    @SerialName("merchant_name") val merchantName: String = "",
    @SerialName("merchant_is_verified") val merchantIsVerified: Boolean = false
)

@Serializable
data class ProductImage(
    val id: String,
    val url: String,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
enum class ProductCategory(val displayName: String) {
    @SerialName("diagnostic") DIAGNOSTIC("Diagnostic"),
    @SerialName("rehabilitation") REHABILITATION("Rehabilitation"),
    @SerialName("life_support") LIFE_SUPPORT("Life Support"),
    @SerialName("mobility") MOBILITY("Mobility"),
    @SerialName("sterilization") STERILIZATION("Sterilization"),
    @SerialName("monitoring") MONITORING("Monitoring")
}

@Serializable
data class ProductPage(
    val items: List<Product>,
    val total: Int
)
