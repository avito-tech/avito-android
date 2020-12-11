package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.utils.logging.CILogger
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.io.File

internal class PlainTextBuildVerdictWriter(
    private val buildVerdictDir: Provider<Directory>,
    private val logger: CILogger
) : BuildVerdictWriter {

    override fun write(buildVerdict: BuildVerdict) {
        val dir = buildVerdictDir.get().asFile.apply { mkdirs() }
        val file = File(dir, buildVerdictFileName)
        file.createNewFile()
        file.writeText(
            when (buildVerdict) {
                is BuildVerdict.Execution -> buildVerdict.plainText()
                is BuildVerdict.Configuration -> buildVerdict.plainText()
            }
        )
        logger.warn("Pretty build verdict at $file")
    }

    companion object {
        const val buildVerdictFileName = "pretty-build-verdict.txt"
    }
}
