package com.avito.emcee.queue

import com.avito.emcee.queue.workercapability.WorkerCapabilityRequirement
import com.avito.emcee.queue.workercapability.defaultCapabilityRequirements
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class SimilarlyConfiguredTestEntries(
    val testEntryConfiguration: TestEntryConfiguration,
    val testEntries: List<TestEntry>,
) {
    @JsonClass(generateAdapter = true)
    public data class TestEntryConfiguration(
        val testConfigurationContainer: TestConfigurationContainer,
        val testExecutionBehavior: TestExecutionBehavior,
        val workerCapabilityRequirements: List<WorkerCapabilityRequirement> = defaultCapabilityRequirements(),
        val analyticsConfiguration: Any = Any()
    )
}
