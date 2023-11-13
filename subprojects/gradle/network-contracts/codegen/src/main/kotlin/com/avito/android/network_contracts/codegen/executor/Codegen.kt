package com.avito.android.network_contracts.codegen.executor

import com.avito.android.Result
import com.avito.android.network_contracts.codegen.Arch
import com.avito.android.network_contracts.shared.runCommand
import com.avito.logger.Logger
import com.avito.utils.ProcessRunner
import org.gradle.api.file.FileCollection
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute

internal interface Codegen {
    fun execute(
        crtEnv: Pair<String, Path>,
        keyEnv: Pair<String, Path>,
        skipValidation: Boolean
    ): Result<String>

    class Impl(
        private val arch: Arch,
        private val codegenBinaryFiles: FileCollection,
        private val logger: Logger,
        private val srcDir: File,
        private val name: String,
        private val kind: String,
    ) : Codegen {

        private val codegenFile = findCodegenBinaryFileForArch()

        override fun execute(
            crtEnv: Pair<String, Path>,
            keyEnv: Pair<String, Path>,
            skipValidation: Boolean
        ): Result<String> {
            val processRunner = ProcessRunner.create(codegenFile.parentFile)
            return processRunner.executeCodegen(
                crtEnv = crtEnv,
                keyEnv = keyEnv,
                codegenFile = codegenFile,
                srcDir = srcDir,
                name = name,
                kind = kind,
                skipValidation = skipValidation
            )
        }

        private fun findCodegenBinaryFileForArch(): File {
            return codegenBinaryFiles.first { it.name.contains(arch.binarySuffix) }
        }

        private fun ProcessRunner.executeCodegen(
            crtEnv: Pair<String, Path>,
            keyEnv: Pair<String, Path>,
            codegenFile: File,
            srcDir: File,
            name: String,
            kind: String,
            skipValidation: Boolean,
        ): Result<String> {
            val rawCommand = buildString {
                append("env ${crtEnv.first}=${crtEnv.second.absolute()} ")
                append("env ${keyEnv.first}=${keyEnv.second.absolute()} ")
                append("./${codegenFile.name} --dir ${srcDir.path} --kind $kind --name $name")
                if (skipValidation) {
                    append(" --skip-validation")
                }
            }

            logger.info("Codegen Command is about to run:")
            logger.info(rawCommand)

            return runCommand(rawCommand)
        }
    }

    companion object {
        internal fun create(
            arch: Arch,
            codegenBinaryFiles: FileCollection,
            logger: Logger,
            srcDir: File,
            name: String,
            kind: String,
        ) = Impl(arch, codegenBinaryFiles, logger, srcDir, name, kind)
    }
}
