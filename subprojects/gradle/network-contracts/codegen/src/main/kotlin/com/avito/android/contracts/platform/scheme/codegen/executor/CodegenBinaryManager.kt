package com.avito.android.contracts.platform.scheme.codegen.executor

import com.avito.android.Result
import com.avito.android.contracts.platform.scheme.codegen.config.CodegenConfig
import com.avito.logger.Logger
import org.gradle.api.file.FileCollection
import org.gradle.internal.os.OperatingSystem

internal class CodegenBinaryManager(
    private val executableFiles: FileCollection,
    private val logger: Logger,
) {

    internal fun createCodegenInstance(config: CodegenConfig): Result<Codegen> {
        val binaryName = binaryName()
        val execFile = executableFiles.firstOrNull { it.name.contains(binaryName) }
            ?: return Result.Failure<Codegen>(IllegalStateException("Unable to find executable file for $binaryName"))

        val codegen = Codegen.Impl(execFile, logger, config)
        return Result.Success(codegen)
    }

    private fun binaryName(): String {
        val os = OperatingSystem.current()
        val arch = System.getProperty("os.arch")
        val prefix = when {
            os.isLinux -> "linux"
            os.isMacOsX -> "darwin"
            else -> os.familyName
        }

        val archSuffix = when (arch) {
            "x86_64", "amd64" -> "amd64"
            "aarch64", "arm64" -> "arm64"
            else -> arch
        }

        return "${prefix.lowercase()}_${archSuffix.lowercase()}"
    }
}
