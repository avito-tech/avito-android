package com.avito.android.plugins.configuration

import com.avito.reportviewer.model.RunId
import com.avito.time.TimeProvider
import java.util.concurrent.TimeUnit

public class RunIdResolver(
    private val timeProvider: TimeProvider,
    private val gitResolver: GitResolver,
    private val buildEnvResolver: BuildEnvResolver,
) {

    public fun getCiRunId(reportRunIdPrefix: String?): RunId {

        val gitCommitHash = gitResolver.getGitCommit().orNull

        return if (gitCommitHash != null) {
            RunId(
                prefix = reportRunIdPrefix,
                identifier = gitResolver.getGitCommit().getOrElse("local"),
                buildTypeId = buildEnvResolver.getBuildType()
            )
        } else {
            getLocalRunId()
        }
    }

    public fun getLocalRunId(): RunId {
        return RunId(
            prefix = null,
            identifier = TimeUnit.MILLISECONDS.toDays(timeProvider.nowInMillis()).toString(),
            buildTypeId = "LOCAL"
        )
    }
}
