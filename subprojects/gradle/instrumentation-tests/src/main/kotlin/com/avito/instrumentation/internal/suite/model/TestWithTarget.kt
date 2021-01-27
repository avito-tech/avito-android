package com.avito.instrumentation.internal.suite.model

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.report.model.TestStaticData

data class TestWithTarget(
    val test: TestStaticData,
    val target: TargetConfiguration.Data
)

// todo чето как-то уродливо, кажется проблема в том что targetTestRun слишком много знает,
//  и нужно стркутуру саму поменять
internal fun List<TestWithTarget>.transformTestsWithNewJobSlug(newJobSlug: String): List<TestWithTarget> {
    return map {
        TestWithTarget(
            test = it.test,
            target = it.target.copy(
                instrumentationParams = it.target.instrumentationParams.applyParameters(
                    mapOf("jobSlug" to newJobSlug)
                )
            )
        )
    }
}
