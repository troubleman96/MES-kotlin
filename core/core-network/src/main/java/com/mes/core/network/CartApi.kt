package com.mes.core.network

import com.mes.core.network.envelope.Envelope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.*

interface CartApi {
    @GET("cart/")
    suspend fun getCart(): Envelope<CartData>

    @PATCH("cart/")
    suspend fun syncCart(@Body request: CartSyncRequest): Envelope<CartResponse>
}

@Serializable
data class CartData(
    val lines: List<com.mes.core.domain.CartLine>
)

@Serializable
data class CartSyncRequest(
    val lines: List<com.mes.core.domain.CartLine>
)

@Serializable
data class CartResponse(
    val cart: CartData,
    @SerialName("stale_lines") val staleLines: List<JsonElement> = emptyList()
)
