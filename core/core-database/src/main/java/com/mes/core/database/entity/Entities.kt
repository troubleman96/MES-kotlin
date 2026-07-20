package com.mes.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_lines")
data class CartLineEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val merchantId: String,
    val merchantName: String,
    val productName: String,
    val thumbnailUrl: String,
    val dailyRateTzs: Long,
    val rentalStartDate: String,
    val rentalEndDate: String,
    val quantity: Int = 1,
    val addedAt: String,
    val syncState: String = "PENDING"
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val label: String,
    val facilityName: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val city: String,
    val contactName: String,
    val contactPhone: String,
    val deliveryNotes: String? = null,
    val isDefault: Boolean = false,
    val addressType: String = "DELIVERY"
)

@Entity(tableName = "products_cache")
data class ProductCacheEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val merchantId: String,
    val merchantName: String,
    val merchantIsVerified: Boolean,
    val dailyRateTzs: Long,
    val weeklyRateTzs: Long? = null,
    val monthlyRateTzs: Long? = null,
    val imageUrls: String,
    val specs: String,
    val isFeatured: Boolean,
    val isAvailable: Boolean,
    val availableFrom: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
