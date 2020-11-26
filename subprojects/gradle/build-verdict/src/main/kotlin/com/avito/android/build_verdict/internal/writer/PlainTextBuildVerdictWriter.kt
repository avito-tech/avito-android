package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.utils.logging.CILogger
import java.io.File

internal class PlainTextBuildVerdictWriter(
    private val buildVerdictDir: File,
    private val logger: CILogger
) : BuildVerdictWriter {

    override fun write(buildVerdict: BuildVerdict) {
        val dir = buildVerdictDir.apply { mkdirs() }
        val file = File(dir, buildVerdictFileName)
        file.createNewFile()
        file.writeText(
            """
Your build FAILED: "${buildVerdict.rootError.message}"
-------------------------------------------------------

Failed tasks:
${
                StringBuilder().apply {
                    val lineSeparator = System.lineSeparator()
                    buildVerdict.failedTasks.forEachIndexed { index, failedTask ->
                        append("${index + 1}: Task ${failedTask.projectPath}:${failedTask.name}$lineSeparator")
                        append("* What went wrong:$lineSeparator")
                        append("${failedTask.errorOutput.trimIndent()}$lineSeparator")
                        append("________________________________________")
                        append(lineSeparator)
                    }
                }
            }""".trimMargin()
        )
        logger.warn("Pretty build verdict at $file")
    }

    companion object {
        const val buildVerdictFileName = "pretty-build-verdict.txt"
    }
}
