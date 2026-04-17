package com.pomodoroalert.data

import com.google.gson.annotations.SerializedName

/**
 * DTO matching the cloud webhook schema.
 */
data class WebhookPayload(
    @SerializedName("log_id") val logId: String,
    @SerializedName("task_name") val taskName: String,
    @SerializedName("plan_duration") val planDuration: Int,
    @SerializedName("actual_status") val actualStatus: String,
    @SerializedName("trigger_source") val triggerSource: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("voice_id") val voiceId: String
)
