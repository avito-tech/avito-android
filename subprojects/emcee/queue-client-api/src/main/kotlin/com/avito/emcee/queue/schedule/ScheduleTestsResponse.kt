package com.avito.emcee.queue.schedule

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ScheduleTestsResponse(
    val responseType: String // TODO: use polymorphic adapter
)
