package com.mes.core.network

import com.mes.core.network.envelope.Envelope
import kotlinx.serialization.SerialName
import retrofit2.http.*

interface CatalogApi {
    @GET("products/")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Envelope<com.mes.core.domain.ProductPage>

    @GET("products/{id}/")
    suspend fun getProduct(@Path("id") id: String): Envelope<com.mes.core.domain.Product>

    @GET("products/{id}/availability/")
    suspend fun getProductAvailability(@Path("id") id: String): Envelope<AvailabilityResponse>

    @GET("merchants/")
    suspend fun getMerchants(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50,
        @Query("search") search: String? = null
    ): Envelope<MerchantPage>

    @GET("merchants/{id}/")
    suspend fun getMerchant(@Path("id") id: String): Envelope<com.mes.core.domain.User>

    @GET("merchants/me/products/")
    suspend fun getMyProducts(): Envelope<com.mes.core.domain.ProductPage>

    @GET("merchants/{id}/products/")
    suspend fun getMerchantProducts(@Path("id") id: String): Envelope<com.mes.core.domain.ProductPage>

    @POST("products/")
    suspend fun createProduct(@Body request: CreateProductRequest): Envelope<com.mes.core.domain.Product>
}

@kotlinx.serialization.Serializable
data class CreateProductRequest(
    val name: String,
    val description: String,
    @SerialName("daily_rate_tzs") val dailyRateTzs: Long,
    val category: String,
    val stock: Int = 1,
    val specs: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap()
)

@kotlinx.serialization.Serializable
data class MerchantPage(
    val items: List<com.mes.core.domain.User>,
    val total: Int = 0,
    @SerialName("count") val count: Int = 0,
    @SerialName("counts") val counts: Int = 0
)

@kotlinx.serialization.Serializable
data class AvailabilityResponse(
    @kotlinx.serialization.SerialName("blocked_ranges")
    val blockedRanges: List<DateRange>
)

@kotlinx.serialization.Serializable
data class DateRange(
    @kotlinx.serialization.SerialName("start_date")
    val startDate: String,
    @kotlinx.serialization.SerialName("end_date")
    val endDate: String
)
