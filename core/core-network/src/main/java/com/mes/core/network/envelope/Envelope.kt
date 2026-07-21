package com.mes.core.network.envelope

import kotlinx.serialization.Serializable

@Serializable
data class Envelope<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val meta: PageMeta? = null
)

@Serializable
data class ApiError(
    val code: String = "",
    val message: String = ""
)

@Serializable
data class PageMeta(
    val page: Int = 1,
    @kotlinx.serialization.SerialName("per_page") val perPage: Int = 20,
    @kotlinx.serialization.SerialName("total_pages") val totalPages: Int = 1,
    @kotlinx.serialization.SerialName("total_items") val totalItems: Int = 0
)

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val code: String, val message: String) : ApiResult<Nothing>
    data object NetworkError : ApiResult<Nothing>
}

suspend fun <T> safeApiCall(block: suspend () -> Envelope<T>): ApiResult<T> = try {
    val envelope = block()
    if (envelope.success) {
        if (envelope.data != null) {
            ApiResult.Success(envelope.data)
        } else {
            // Handle success: true but data: null (e.g. 204 equivalent)
            // If T is Unit, this is fine. If T is something else, it might be an error or empty state.
            ApiResult.Failure(code = "empty_data", message = "No data received")
        }
    } else {
        ApiResult.Failure(
            code = envelope.error?.code ?: "unknown",
            message = envelope.error?.message ?: "Unknown error"
        )
    }
} catch (e: java.io.IOException) {
    ApiResult.NetworkError
} catch (e: kotlinx.serialization.SerializationException) {
    ApiResult.Failure(code = "serialization_error", message = "Data format error: ${e.message}")
} catch (e: retrofit2.HttpException) {
    val errorBody = e.response()?.errorBody()?.string()
    val apiError = try {
        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .decodeFromString<Envelope<Unit>>(
                errorBody ?: """{"success":false,"error":{"code":"http_error","message":"HTTP ${e.code()}"}}"""
            ).error
    } catch (_: Exception) {
        null
    }
    ApiResult.Failure(
        code = apiError?.code ?: "http_error",
        message = apiError?.message ?: "HTTP ${e.code()}"
    )
} catch (e: Exception) {
    ApiResult.Failure(code = "unexpected", message = e.message ?: "Unexpected error")
}
