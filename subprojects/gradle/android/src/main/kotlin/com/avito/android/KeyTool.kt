package com.avito.android

import com.avito.utils.ExistingFile
import com.avito.utils.ProcessRunner
import java.time.Duration

public class KeyTool(private val processRunner: ProcessRunner) {

    private val signatureRegex = Regex("SHA1: ([A-F0-9:]+)")

    public fun getJarSha1(jarFile: ExistingFile): Result<String> {
        return processRunner.run(
            command = "keytool -printcert -jarfile $jarFile",
            timeout = Duration.ofSeconds(60)
        )
            .flatMap {
                val result = signatureRegex.find(it)
                    ?.groupValues
                    ?.get(1)
                    ?.replace(":", "")
                    ?.lowercase()

                if (result != null) {
                    Result.Success(result)
                } else {
                    Result.Failure(IllegalStateException("Can't parse signature"))
                }
            }
    }
}
