package com.mes.core.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String? = null,
    val label: String,
    @SerialName("facility_name") val facilityName: String,
    @SerialName("address_line1") val addressLine1: String,
    @SerialName("address_line2") val addressLine2: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val city: String,
    @SerialName("contact_name") val contactName: String,
    @SerialName("contact_phone") val contactPhone: String,
    @SerialName("delivery_notes") val deliveryNotes: String? = null,
    @SerialName("address_type") val addressType: AddressType = AddressType.BOTH,
    @SerialName("is_default") val isDefault: Boolean = false
)

@Serializable
enum class AddressType {
    @SerialName("delivery") DELIVERY,
    @SerialName("billing") BILLING,
    @SerialName("both") BOTH
}
