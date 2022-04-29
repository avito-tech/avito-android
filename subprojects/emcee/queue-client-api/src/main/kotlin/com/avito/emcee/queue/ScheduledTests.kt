package com.avito.emcee.queue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ScheduledTests(
    @Json(name = "testEntryConfiguration")
    val config: Config,
    val testEntries: List<TestEntry>
) {
    @JsonClass(generateAdapter = true)
    public data class Config(
        @Json(name = "testConfigurationContainer")
        val testConfiguration: TestConfiguration,
        /**
         * Unused. Should be optional
         */
        val workerCapabilityRequirements: List<Any> = emptyList(),
        /**
         * Unused. Should be optional
         */
        val analyticsConfiguration: Any = Any()
    )
}
