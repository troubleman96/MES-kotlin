package com.mes.core.network

import com.mes.core.domain.Order
import com.mes.core.network.envelope.Envelope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.*

interface OrdersApi {
    @POST("checkout/")
    suspend fun checkout(@Body request: CheckoutRequest): Envelope<CheckoutResponse>

    @GET("orders/")
    suspend fun getOrders(
        @Query("status") status: String? = null
    ): Envelope<List<Order>>

    @GET("orders/{id}/")
    suspend fun getOrder(@Path("id") id: String): Envelope<Order>

    @PATCH("orders/{id}/status/")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: StatusUpdateRequest
    ): Envelope<Order>

    @POST("orders/{id}/pay/")
    suspend fun initiatePayment(@Path("id") id: String): Envelope<PaymentIntentResponse>

    @GET("orders/{id}/payment-status/")
    suspend fun getPaymentStatus(@Path("id") id: String): Envelope<PaymentStatusResponse>
}

@Serializable
data class CheckoutRequest(
    @SerialName("delivery_address_id") val deliveryAddressId: String,
    @SerialName("billing_address_id") val billingAddressId: String,
    val notes: String? = null
)

@Serializable
data class CheckoutResponse(
    @SerialName("order_group_id") val orderGroupId: String,
    @SerialName("sub_orders") val subOrders: List<SubOrderMinimal>
)

@Serializable
data class SubOrderMinimal(
    val id: String,
    @SerialName("merchant_name") val merchantName: String,
    @SerialName("subtotal_tzs") val subtotalTzs: Long,
    val status: String
)

@Serializable
data class StatusUpdateRequest(
    val status: String
)

@Serializable
data class PaymentIntentResponse(
    val id: String? = null,
    @SerialName("snippe_reference") val snippeReference: String,
    val status: String,
    @SerialName("amount_tzs") val amountTzs: Long,
    @SerialName("expires_at") val expiresAt: String
)

@Serializable
data class PaymentStatusResponse(
    val status: String,
    @SerialName("failure_reason") val failureReason: String? = null
)
