package com.avito.android.build_verdict.internal

import org.gradle.BuildResult

internal class BuildConfigurationFailureListener(
    private val listener: BuildFailedListener
) : BaseBuildListener() {

    override fun buildFinished(result: BuildResult) {
        result.failure?.let { failure ->
            listener.onFailed(
                BuildVerdict.Configuration(
                    error = Error.from(failure)
                )
            )
        }
    }
}
