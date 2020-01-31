package com.avito.android

import com.avito.utils.ExistingDirectory
import com.avito.utils.ExistingFile
import com.avito.utils.ProcessRunner
import org.funktionale.tries.Try

interface ApkSigner {

    fun getApkSha1(apk: ExistingFile): Try<String>

    class Impl(buildToolsPath: ExistingDirectory, private val processRunner: ProcessRunner) : ApkSigner {

        private val signatureRegex: Regex = Regex("SHA-1 digest: ([a-fA-F0-9]+)", RegexOption.MULTILINE)

        private val apkSignerPath: ExistingFile = buildToolsPath.file("/apksigner")

        override fun getApkSha1(apk: ExistingFile): Try<String> {
            return processRunner.run("$apkSignerPath verify --print-certs $apk")
                .flatMap {
                    val result = signatureRegex.find(it)
                        ?.groupValues
                        ?.get(1)

                    if (result != null) {
                        Try.Success(result)
                    } else {
                        Try.Failure<String>(IllegalStateException("Can't parse signature"))
                    }
                }
        }
    }
}
