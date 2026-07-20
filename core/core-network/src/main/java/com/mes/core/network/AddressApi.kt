package com.mes.core.network

import com.mes.core.domain.Address
import com.mes.core.network.envelope.Envelope
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AddressApi {
    @GET("api/v1/addresses/")
    suspend fun getAddresses(): Envelope<List<Address>>

    @POST("api/v1/addresses/")
    suspend fun createAddress(@Body request: Address): Envelope<Address>

    @PUT("api/v1/addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body request: Address): Envelope<Address>

    @DELETE("api/v1/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Envelope<Unit>
}
