package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.internal.writer.BuildVerdictWriter

internal class BuildFailedListener(
    private val writers: List<BuildVerdictWriter>
) {
    fun onFailed(verdict: BuildVerdict) {
        writers.forEach { writer ->
            writer.write(verdict)
        }
    }
}
