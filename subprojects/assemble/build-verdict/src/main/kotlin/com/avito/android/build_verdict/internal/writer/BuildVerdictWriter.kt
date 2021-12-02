package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import org.slf4j.Logger
import java.io.File

internal abstract class BuildVerdictWriter(
    private val outputDir: Lazy<File>,
    private val fileName: String
) {
    protected abstract val logger: Logger

    fun write(buildVerdict: BuildVerdict) {
        val destination = File(outputDir.value, fileName)
        destination.createNewFile()
        writeTo(buildVerdict, destination)
        logger.warn("Build verdict at $destination")
    }

    protected abstract fun writeTo(buildVerdict: BuildVerdict, destination: File)
}
