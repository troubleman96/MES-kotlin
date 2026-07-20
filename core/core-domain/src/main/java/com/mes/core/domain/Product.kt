package com.mes.core.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val category: ProductCategory,
    val merchantId: String,
    val merchantName: String,
    val merchantIsVerified: Boolean = false,
    val dailyRateTzs: Long,
    val weeklyRateTzs: Long? = null,
    val monthlyRateTzs: Long? = null,
    val imageUrls: List<String> = emptyList(),
    val specs: Map<String, String> = emptyMap(),
    val isFeatured: Boolean = false,
    val isAvailable: Boolean = true,
    val availableFrom: LocalDate? = null
)

@Serializable
enum class ProductCategory(val displayName: String) {
    DIAGNOSTIC("Diagnostic"),
    REHABILITATION("Rehabilitation"),
    LIFE_SUPPORT("Life Support"),
    MOBILITY("Mobility"),
    STERILIZATION("Sterilization"),
    MONITORING("Monitoring")
}

@Serializable
data class ProductPage(
    val items: List<Product>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Int
)
