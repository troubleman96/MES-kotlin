package com.mes.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String,
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
    val addressType: AddressType = AddressType.DELIVERY
)

@Serializable
enum class AddressType {
    DELIVERY,
    BILLING,
    BOTH
}
