package com.mes.core.network

import com.mes.core.network.envelope.Envelope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register/")
    suspend fun register(@Body request: RegisterRequest): Envelope<AuthResponse>

    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): Envelope<AuthResponse>

    @POST("auth/verify-phone/")
    suspend fun verifyPhone(@Body request: OtpRequest): Envelope<AuthResponse>

    @POST("auth/send-phone-otp/")
    suspend fun sendPhoneOtp(): Envelope<Unit>

    @GET("auth/me/")
    suspend fun getProfile(): Envelope<com.mes.core.domain.User>
}

@Serializable
data class RegisterRequest(
    val email: String,
    val phone: String,
    val password: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val role: String,
    @SerialName("business_name") val businessName: String? = null,
    @SerialName("facility_name") val facilityName: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class OtpRequest(
    val phone: String,
    val otp: String
)

@Serializable
data class AuthResponse(
    @SerialName("id") val userId: String? = null,
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Int? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("phone_verified") val phoneVerified: Boolean? = null,
    @SerialName("profile_complete") val profileComplete: Boolean? = null
)
