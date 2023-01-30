package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class SimilarlyConfiguredTestEntries(
    val testEntryConfiguration: TestEntryConfiguration,
    val testEntries: List<TestEntry>
) {
    @JsonClass(generateAdapter = true)
    public data class TestEntryConfiguration(
        val testConfigurationContainer: TestConfigurationContainer,
        val testExecutionBehavior: TestExecutionBehavior,
        val workerCapabilityRequirements: List<Any> = emptyList(),
        val analyticsConfiguration: Any = Any()
    )
}
