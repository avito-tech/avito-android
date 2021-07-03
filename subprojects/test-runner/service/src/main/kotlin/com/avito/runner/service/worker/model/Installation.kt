package com.avito.runner.service.worker.model

public data class Installation(
    val application: String,
    val timestampStartedMilliseconds: Long,
    val timestampCompletedMilliseconds: Long
)
