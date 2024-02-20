package com.avito.android.network_contracts.codegen.executor

import com.avito.android.Result
import com.avito.android.network_contracts.codegen.Arch
import com.avito.android.network_contracts.codegen.config.CodegenConfig
import com.avito.android.network_contracts.codegen.config.args
import com.avito.android.network_contracts.codegen.config.envVars
import com.avito.logger.Logger
import com.avito.utils.ProcessRunner
import org.gradle.api.file.FileCollection
import java.io.File
import java.nio.file.Path

internal interface Codegen {
    fun execute(
        crtEnv: Pair<String, Path>,
        keyEnv: Pair<String, Path>,
        skipValidation: Boolean
    ): Result<String>

    class Impl(
        private val codegenFile: File,
        private val logger: Logger,
        private val config: CodegenConfig,
    ) : Codegen {

        override fun execute(
            crtEnv: Pair<String, Path>,
            keyEnv: Pair<String, Path>,
            skipValidation: Boolean
        ): Result<String> {
            val processRunner = ProcessRunner.create(codegenFile.parentFile)
            return processRunner.executeCodegen(config)
        }

        private fun ProcessRunner.executeCodegen(
            config: CodegenConfig,
        ): Result<String> {
            val rawCommand = buildString {
                val envVariables = config.envVars.joinToString(
                    separator = " ",
                    postfix = " ",
                    transform = { "env ${it.first}=${it.second}" }
                )
                append(envVariables)

                append("./${codegenFile.name}")

                val arguments = config.args.joinToString(
                    separator = " ",
                    prefix = " ",
                    transform = { "--${it.first} ${it.second?.let { value -> "\'$value\'" }.orEmpty()}" }
                )
                append(arguments)
            }

            logger.info("Codegen Command is about to run:")
            logger.info(rawCommand)

            return run(rawCommand)
        }
    }

    companion object {
        internal fun create(
            arch: Arch,
            codegenBinaryFiles: FileCollection,
            logger: Logger,
            config: CodegenConfig,
        ): Codegen {
            val codegenFile = findCodegenBinaryFileForArch(arch, codegenBinaryFiles)
            return Impl(codegenFile, logger, config)
        }

        private fun findCodegenBinaryFileForArch(
            arch: Arch,
            codegenBinaryFiles: FileCollection,
        ): File {
            return codegenBinaryFiles.first { it.name.contains(arch.binarySuffix) }
        }
    }
}
