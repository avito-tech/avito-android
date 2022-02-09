package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ScheduleTestsBody(
    val prioritizedJob: Job,
    val scheduleStrategy: ScheduleStrategy,
    @Json(name = "testEntryConfigurations")
    val tests: List<TestRequest>
)
