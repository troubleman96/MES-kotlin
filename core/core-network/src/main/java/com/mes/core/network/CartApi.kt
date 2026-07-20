package com.mes.core.network

import com.mes.core.network.envelope.Envelope
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApi {
    @GET("api/v1/cart/")
    suspend fun getCart(): Envelope<List<com.mes.core.domain.CartLine>>

    @POST("api/v1/cart/items")
    suspend fun addItem(@Body request: AddCartLineRequest): Envelope<com.mes.core.domain.CartLine>

    @PATCH("api/v1/cart/items/{itemId}")
    suspend fun updateItem(
        @Path("itemId") itemId: String,
        @Body request: UpdateCartLineRequest
    ): Envelope<com.mes.core.domain.CartLine>

    @DELETE("api/v1/cart/items/{itemId}")
    suspend fun removeItem(@Path("itemId") itemId: String): Envelope<Unit>

    @DELETE("api/v1/cart/")
    suspend fun clearCart(): Envelope<Unit>
}

data class AddCartLineRequest(
    val productId: String,
    val rentalStart: String,
    val rentalEnd: String,
    val quantity: Int = 1
)

data class UpdateCartLineRequest(
    val rentalStart: String? = null,
    val rentalEnd: String? = null,
    val quantity: Int? = null
)
