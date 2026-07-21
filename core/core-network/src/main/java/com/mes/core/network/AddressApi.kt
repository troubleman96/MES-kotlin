package com.mes.core.network

import com.mes.core.domain.Address
import com.mes.core.network.envelope.Envelope
import retrofit2.http.*

interface AddressApi {
    @GET("addresses")
    suspend fun getAddresses(): Envelope<List<Address>>

    @POST("addresses")
    suspend fun createAddress(@Body request: Address): Envelope<Address>

    @PUT("addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body request: Address): Envelope<Address>

    @DELETE("addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Envelope<Unit>
}
