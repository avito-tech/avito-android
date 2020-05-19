package com.avito.performance

import org.gradle.api.model.ObjectFactory
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
}
