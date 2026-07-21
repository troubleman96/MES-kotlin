package com.mes.core.network

import com.mes.core.network.envelope.Envelope
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface CartApi {
    @GET("cart")
    suspend fun getCart(): Envelope<CartSyncRequest>

    @PATCH("cart")
    suspend fun syncCart(@Body request: CartSyncRequest): Envelope<CartSyncRequest>
}

@Serializable
data class CartSyncRequest(
    val lines: List<com.mes.core.domain.CartLine>
)
