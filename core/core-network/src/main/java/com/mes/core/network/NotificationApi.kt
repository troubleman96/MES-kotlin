package com.mes.core.network

import com.mes.core.domain.Notification
import com.mes.core.network.envelope.Envelope
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("api/v1/notifications")
    suspend fun list(
        @Query("page") page: Int = 1,
        @Query("unread") unreadOnly: Boolean? = null
    ): Envelope<NotificationPage>

    @GET("api/v1/notifications/unread-count")
    suspend fun unreadCount(): Envelope<UnreadCount>

    @PATCH("api/v1/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): Envelope<Unit>

    @PATCH("api/v1/notifications/read-all")
    suspend fun markAllRead(): Envelope<Unit>
}

data class NotificationPage(
    val items: List<Notification>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Int
)

data class UnreadCount(val count: Int)
