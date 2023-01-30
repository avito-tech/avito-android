package com.avito.emcee.queue.schedule

import com.avito.emcee.queue.Job
import com.avito.emcee.queue.ScheduleStrategy
import com.avito.emcee.queue.SimilarlyConfiguredTestEntries
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ScheduleTestsBody(
    val prioritizedJob: Job,
    val scheduleStrategy: ScheduleStrategy,
    val similarlyConfiguredTestEntries: SimilarlyConfiguredTestEntries
)
