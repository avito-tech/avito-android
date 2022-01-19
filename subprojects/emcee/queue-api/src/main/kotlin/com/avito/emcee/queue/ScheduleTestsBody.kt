package com.avito.emcee.queue

import com.squareup.moshi.Json

public data class ScheduleTestsBody(
    val prioritizedJob: Job,
    val scheduleStrategy: ScheduleStrategy,
    @Json(name = "testEntryConfigurations")
    val tests: List<TestRequest>
)
