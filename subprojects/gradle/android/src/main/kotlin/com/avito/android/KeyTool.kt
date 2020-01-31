package com.avito.android

import com.avito.utils.ExistingFile
import com.avito.utils.ProcessRunner
import org.funktionale.tries.Try

class KeyTool(private val processRunner: ProcessRunner) {

    private val signatureRegex = Regex("SHA1: ([A-F0-9:]+)")

    fun getJarSha1(jarFile: ExistingFile): Try<String> {
        return processRunner.run("keytool -printcert -jarfile $jarFile")
            .flatMap {
                val result = signatureRegex.find(it)
                    ?.groupValues
                    ?.get(1)
                    ?.replace(":", "")
                    ?.toLowerCase()

                if (result != null) {
                    Try.Success(result)
                } else {
                    Try.Failure<String>(IllegalStateException("Can't parse signature"))
                }
            }
    }
}
