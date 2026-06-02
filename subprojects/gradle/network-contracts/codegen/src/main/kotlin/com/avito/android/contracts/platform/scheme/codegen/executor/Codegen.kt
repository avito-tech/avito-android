package com.avito.android.contracts.platform.scheme.codegen.executor

import com.avito.android.Result
import com.avito.android.contracts.platform.scheme.codegen.config.CodegenConfig
import com.avito.android.contracts.platform.scheme.codegen.config.args
import com.avito.android.contracts.platform.scheme.codegen.config.envVars
import com.avito.logger.Logger
import com.avito.utils.ProcessRunner
import java.io.File

internal interface Codegen {
    fun execute(
        skipValidation: Boolean
    ): Result<String>

    fun update(): Result<String>

    class Impl(
        private val codegenFile: File,
        private val logger: Logger,
        private val config: CodegenConfig,
    ) : Codegen {

        override fun execute(
            skipValidation: Boolean
        ): Result<String> {
            val processRunner = ProcessRunner.create(codegenFile.parentFile)
            return processRunner.executeCodegen(config)
        }

        override fun update(): Result<String> {
            val processRunner = ProcessRunner.create(codegenFile.parentFile)
            val generators = config.generators.joinToString(separator = " ") { "-g $it" }
            return processRunner.executeCodegen(config, extraArgs = "--update $generators")
        }

        private fun ProcessRunner.executeCodegen(
            config: CodegenConfig,
            extraArgs: String = "",
        ): Result<String> {
            val rawCommand = buildString {
                val envVariables = config.envVars.joinToString(
                    separator = " ",
                    postfix = " ",
                    transform = { "env ${it.first}=${it.second}" }
                )
                append(envVariables)

                append("${codegenFile.path}")

                val arguments = config.args.joinToString(
                    separator = " ",
                    prefix = " ",
                    transform = { "--${it.first} ${it.second?.let { value -> "\'$value\'" }.orEmpty()}" }
                )
                append(arguments)
                append(" $extraArgs")
            }

            logger.debug("Codegen Command is about to run: $rawCommand")

            return run(rawCommand, timeout = config.timeout)
        }
    }
}
