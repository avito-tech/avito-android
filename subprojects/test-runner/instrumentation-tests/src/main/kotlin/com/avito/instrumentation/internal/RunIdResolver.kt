package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration
import com.avito.report.model.RunId
import com.avito.time.TimeProvider
import org.gradle.api.Project
import java.util.concurrent.TimeUnit

internal class RunIdResolver(
    private val timeProvider: TimeProvider,
    private val project: Project,
) {

    fun getCiRunId(
        extension: InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
    ): RunId {

        val gitCommitHash = GitResolver.getGitCommit(project).orNull

        return if (gitCommitHash != null) {
            RunId(
                prefix = extension.testReport.reportViewer?.reportRunIdPrefix,
                identifier = GitResolver.getGitCommit(project).getOrElse("local"),
                buildTypeId = BuildEnvResolver.getBuildType(project)
            )
        } else {
            getLocalRunId(timeProvider)
        }
    }

    private fun getLocalRunId(timeProvider: TimeProvider): RunId {
        return RunId(
            prefix = null,
            identifier = TimeUnit.MILLISECONDS.toDays(timeProvider.nowInMillis()).toString(),
            buildTypeId = "LOCAL"
        )
    }
}
