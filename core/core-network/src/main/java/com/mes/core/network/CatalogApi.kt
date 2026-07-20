package com.mes.core.network

import com.mes.core.network.envelope.Envelope
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogApi {
    @GET("api/v1/equipment/")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("featured") featured: Boolean? = null
    ): Envelope<com.mes.core.domain.ProductPage>

    @GET("api/v1/equipment/{id}")
    suspend fun getProduct(@Path("id") id: String): Envelope<com.mes.core.domain.Product>

    @GET("api/v1/equipment/featured")
    suspend fun getFeaturedProducts(): Envelope<List<com.mes.core.domain.Product>>
}
