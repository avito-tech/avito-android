package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.reportviewer.model.RunId
import com.avito.time.TimeProvider
import java.util.concurrent.TimeUnit

internal class RunIdResolver(
    private val timeProvider: TimeProvider,
    private val gitResolver: GitResolver,
    private val buildEnvResolver: BuildEnvResolver,
) {

    fun getCiRunId(extension: InstrumentationTestsPluginExtension): RunId {

        val gitCommitHash = gitResolver.getGitCommit().orNull

        return if (gitCommitHash != null) {
            RunId(
                prefix = extension.testReport.reportViewer?.reportRunIdPrefix,
                identifier = gitResolver.getGitCommit().getOrElse("local"),
                buildTypeId = buildEnvResolver.getBuildType()
            )
        } else {
            getLocalRunId()
        }
    }

    fun getLocalRunId(): RunId {
        return RunId(
            prefix = null,
            identifier = TimeUnit.MILLISECONDS.toDays(timeProvider.nowInMillis()).toString(),
            buildTypeId = "LOCAL"
        )
    }
}
