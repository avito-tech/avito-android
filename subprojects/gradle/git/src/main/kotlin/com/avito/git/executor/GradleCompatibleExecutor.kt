package com.avito.git.executor

import com.avito.android.Result
import org.apache.tools.ant.types.Commandline
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

internal class GradleCompatibleExecutor(
    private val execOperations: ExecOperations,
) : GitExecutor {

    override fun git(command: String): Result<String> {
        val output = ByteArrayOutputStream()
        return Result.tryCatch {
            val result: ExecResult = execOperations.exec { spec ->
                with(spec) {
                    commandLine("git", *Commandline.translateCommandline(command))
                    standardOutput = output
                }
            }
            result.assertNormalExitValue()
            String(output.toByteArray(), Charset.defaultCharset()).trim()
        }.rescue { throwable ->
            throw RuntimeException("Cannot execute git command: git $command", throwable)
        }
    }
}
