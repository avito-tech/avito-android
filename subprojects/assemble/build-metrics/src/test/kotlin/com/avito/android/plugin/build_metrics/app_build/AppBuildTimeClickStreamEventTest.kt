package com.avito.android.plugin.build_metrics.app_build

import com.avito.android.plugin.build_metrics.BuildEnvironment
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.ApplicationType
import com.avito.android.plugin.build_metrics.internal.gradle.app_build.clickstream.AppBuildTimeClickStreamEvent
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

internal class AppBuildTimeClickStreamEventTest {

    @TestFactory
    fun `build environment - event carries its code as dev tool env`(): List<DynamicTest> =
        listOf(
            BuildEnvironment.CI to "ci",
            BuildEnvironment.LOCAL to "local",
            BuildEnvironment.SANDBOX to "sandbox",
        ).map { (environment, code) ->
            dynamicTest("$environment environment - event carries $code as dev tool env") {
                val event = AppBuildTimeClickStreamEvent(
                    duration = 100L,
                    status = "success",
                    appName = "app",
                    appType = ApplicationType.MAIN,
                    devName = "ivanivanov",
                    branchName = "A-1234_test",
                    repoName = "test",
                    environment = environment,
                )

                assertThat(event.params["dev_tool_env"]).isEqualTo(code)
            }
        }
}
