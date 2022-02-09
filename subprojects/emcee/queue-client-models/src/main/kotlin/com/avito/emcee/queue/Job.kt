package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class Job(
    @Json(name = "jobId")
    val id: String,
    /**
     * Uses to prioritize inside the group. Higher will schedule faster. Expected range [0..999]
     */
    @Json(name = "jobPriority")
    val priority: Int,
    /**
     * Uses to connect jobs. Usually [groupId] unique per build
     */
    @Json(name = "jobGroupId")
    val groupId: String,
    /**
     * Higher will schedule faster. Expected range [0..999]
     */
    @Json(name = "jobGroupPriority")
    val groupPriority: Int,
    /**
     * Some analytics from queue about job
     * Must be empty object to skip
     */
    val analyticsConfiguration: Any,
)
