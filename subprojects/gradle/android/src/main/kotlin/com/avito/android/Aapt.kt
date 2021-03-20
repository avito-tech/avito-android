package com.avito.android

import com.avito.utils.ExistingDirectory
import com.avito.utils.ExistingFile
import com.avito.utils.ProcessRunner
import java.io.File

interface Aapt {

    fun getPackageName(apk: File): Result<String>

    class Impl(buildToolsPath: ExistingDirectory, private val processRunner: ProcessRunner) : Aapt {

        private val signatureRegex = Regex("package: name='(\\S+)'")

        private val aaptPath: ExistingFile = buildToolsPath.file("/aapt")

        override fun getPackageName(apk: File): Result<String> {
            return processRunner.run("$aaptPath dump badging $apk")
                .flatMap {
                    val result = signatureRegex.find(it)
                        ?.groupValues
                        ?.get(1)

                    if (result != null) {
                        Result.Success(result)
                    } else {
                        Result.Failure(IllegalStateException("Can't parse signature"))
                    }
                }
        }
    }
}
