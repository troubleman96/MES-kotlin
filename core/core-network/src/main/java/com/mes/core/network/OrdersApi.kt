package com.mes.core.network

import com.mes.core.domain.Order
import com.mes.core.domain.SubOrder
import com.mes.core.network.envelope.Envelope
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrdersApi {
    @GET("api/v1/orders/")
    suspend fun getOrders(
        @Query("page") page: Int = 1,
        @Query("status") status: String? = null
    ): Envelope<List<Order>>

    @GET("api/v1/orders/{orderId}")
    suspend fun getOrder(@Path("orderId") orderId: String): Envelope<Order>

    @POST("api/v1/orders/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): Envelope<List<SubOrder>>

    @GET("api/v1/orders/{subOrderId}/payment-status")
    suspend fun getPaymentStatus(@Path("subOrderId") subOrderId: String): Envelope<PaymentStatusResponse>

    @POST("api/v1/orders/{subOrderId}/pay")
    suspend fun initiatePayment(@Path("subOrderId") subOrderId: String): Envelope<PaymentIntentResponse>
}

data class CheckoutRequest(
    val cartId: String,
    val deliveryAddressId: String,
    val billingAddressId: String? = null,
    val paymentMethod: String,
    val specialInstructions: Map<String, String>? = null
)

data class PaymentIntentResponse(
    val reference: String,
    val status: String,
    val expiresAt: String,
    val network: String? = null
)

data class PaymentStatusResponse(
    val reference: String,
    val status: String,
    val failureReason: String? = null
)
