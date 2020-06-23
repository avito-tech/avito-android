package com.avito.performance

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class PerformanceExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * Name of a directory for performance test results
     */
    val output = objects.property<String>()

    /**
     * Test results file name
     */
    val performanceTestResultName = objects.property<String>()

    /**
     * Performance calculations service url
     */
    val statsUrl = objects.property<String>()

    /**
     * Slack hook url to send urgent info
     */
    val slackHookUrl = objects.property<String>()

    /**
     * build config id used in report coordinates to fetch build on target branch for results comparison
     */
    val targetBuildConfigId = objects.property<String>()

    /**
     * Toggle for different strategies of target branch results fetching
     *
     * TODO remove after 2020.9
     */
    @Suppress("UnstableApiUsage")
    @Deprecated("ignored, FetchFromOtherBuild will be used anyways")
    val targetBranchResultSource: Property<TargetBranchResultSource> = objects.property()

    sealed class TargetBranchResultSource {
        object RunInProcess : TargetBranchResultSource()
        data class FetchFromOtherBuild(val targetBuildConfigId: String) : TargetBranchResultSource()
    }
}
