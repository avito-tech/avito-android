package com.avito.runner.service.worker.model

data class Installation(
    val application: String,
    val timestampStartedMilliseconds: Long,
    val timestampCompletedMilliseconds: Long
) {

    val duration = timestampCompletedMilliseconds - timestampStartedMilliseconds
}
