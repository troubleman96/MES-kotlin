package com.mes.core.network

import com.mes.core.network.envelope.Envelope
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Envelope<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Envelope<AuthResponse>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): Envelope<AuthResponse>

    @POST("api/v1/auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Envelope<Unit>

    @GET("api/v1/auth/me")
    suspend fun getProfile(): Envelope<com.mes.core.domain.User>
}

data class RegisterRequest(
    val email: String,
    val phone: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val businessName: String? = null,
    val businessRegistrationNumber: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class OtpRequest(
    val phone: String,
    val otp: String
)

data class ResendOtpRequest(
    val phone: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: com.mes.core.domain.User,
    val requiresOtpVerification: Boolean = false
)
