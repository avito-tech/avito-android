package com.avito.android.build_verdict

import com.avito.utils.logging.CILogger
import com.google.gson.Gson
import java.io.File

internal interface BuildVerdictWriter {
    fun write(buildVerdict: BuildVerdict)

    class Impl(
        private val gson: Gson,
        private val buildVerdictDir: File,
        private val ciLogger: CILogger
    ) : BuildVerdictWriter {
        override fun write(buildVerdict: BuildVerdict) {
            val verdict = gson.toJson(
                buildVerdict
            )
            val dir = buildVerdictDir.apply { mkdirs() }
            val file = File(dir, buildVerdictFileName)
            file.createNewFile()
            file.writeText(
                verdict
            )
            ciLogger.warn(
                "Build failed. You can find details at $file"
            )
        }
    }

    companion object {
        val buildVerdictFileName = "build-verdict.json"
    }
}
