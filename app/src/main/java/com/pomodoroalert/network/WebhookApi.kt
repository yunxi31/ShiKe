package com.pomodoroalert.network

import com.pomodoroalert.data.WebhookPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface WebhookApi {
    @POST
    suspend fun syncTask(
        @Url url: String = NetworkConstants.MAKE_WEBHOOK_URL,
        @Body payload: WebhookPayload
    ): Response<Unit>
}
