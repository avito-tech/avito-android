package com.avito.android

import com.avito.utils.ExistingFile
import com.avito.utils.ProcessRunner

class KeyTool(private val processRunner: ProcessRunner) {

    private val signatureRegex = Regex("SHA1: ([A-F0-9:]+)")

    fun getJarSha1(jarFile: ExistingFile): Result<String> {
        return processRunner.run("keytool -printcert -jarfile $jarFile")
            .flatMap {
                val result = signatureRegex.find(it)
                    ?.groupValues
                    ?.get(1)
                    ?.replace(":", "")
                    ?.toLowerCase()

                if (result != null) {
                    Result.Success(result)
                } else {
                    Result.Failure(IllegalStateException("Can't parse signature"))
                }
            }
    }
}
