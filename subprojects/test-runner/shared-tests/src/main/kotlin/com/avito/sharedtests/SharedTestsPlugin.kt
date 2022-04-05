package com.avito.sharedtests

import com.avito.android.plugins.configuration.BuildEnvResolver
import com.avito.android.plugins.configuration.GitResolver
import com.avito.android.plugins.configuration.RunIdResolver
import com.avito.git.gitState
import com.avito.time.DefaultTimeProvider
import com.avito.utils.gradle.envArgs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create

public class SharedTestsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<SharedTestsExtension>("sharedTests")
        configureTestTasks(target, extension)
    }

    private fun configureTestTasks(project: Project, extension: SharedTestsExtension) {
        val runIdResolver = RunIdResolver(
            timeProvider = DefaultTimeProvider(),
            buildEnvResolver = BuildEnvResolver(project.provider { project.envArgs }),
            gitResolver = GitResolver(project.gitState())
        )

        project.tasks.withType(Test::class.java) {
            val mandatoryProperties = mapOf(
                // Enables logging
                "robolectric.logging.enabled" to "true",
                // Always adds actual API version to the test names
                "robolectric.alwaysIncludeVariantMarkersInTestName" to "true",

                "runId" to runIdResolver.getCiRunId(extension.reportRunIdPrefix.orNull)
            )
            it.systemProperties(mandatoryProperties)
        }
    }
}
