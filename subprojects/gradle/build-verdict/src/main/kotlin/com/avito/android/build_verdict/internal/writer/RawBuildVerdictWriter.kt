package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.utils.logging.CILogger
import com.google.gson.Gson
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.File

internal class RawBuildVerdictWriter(
    private val gson: Gson,
    private val buildVerdictDir: Provider<Directory>,
    private val logger: CILogger
) : BuildVerdictWriter {
    override fun write(buildVerdict: BuildVerdict) {
        val verdict = gson.toJson(
            buildVerdict
        )
        val dir = buildVerdictDir.get().asFile.apply { mkdirs() }
        val file = File(dir, buildVerdictFileName)
        file.createNewFile()
        file.writeText(
            verdict
        )
        logger.warn("Raw build verdict at $file")
    }

    companion object {
        const val buildVerdictFileName = "raw-build-verdict.json"
    }
}
