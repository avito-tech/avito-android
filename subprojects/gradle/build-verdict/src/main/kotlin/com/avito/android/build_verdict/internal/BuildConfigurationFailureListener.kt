package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.internal.writer.BuildVerdictWriter
import org.gradle.BuildResult

internal class BuildConfigurationFailureListener(
    private val writer: BuildVerdictWriter
) : BaseBuildListener() {

    override fun buildFinished(result: BuildResult) {
        result.failure?.let { failure ->
            writer.write(
                BuildVerdict.Configuration(
                    error = Error.from(failure)
                )
            )
        }
    }
}
