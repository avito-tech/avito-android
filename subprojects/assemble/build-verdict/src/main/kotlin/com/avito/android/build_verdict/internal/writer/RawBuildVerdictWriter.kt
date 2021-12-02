package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.google.gson.Gson
import org.slf4j.Logger
import java.io.File

internal class RawBuildVerdictWriter(
    buildVerdictDir: Lazy<File>,
    private val gson: Gson,
    override val logger: Logger,
) : BuildVerdictWriter(buildVerdictDir, fileName) {

    override fun writeTo(buildVerdict: BuildVerdict, destination: File) {
        val verdict = gson.toJson(
            buildVerdict
        )
        destination.writeText(
            verdict
        )
    }

    companion object {
        const val fileName = "raw-build-verdict.json"
    }
}
