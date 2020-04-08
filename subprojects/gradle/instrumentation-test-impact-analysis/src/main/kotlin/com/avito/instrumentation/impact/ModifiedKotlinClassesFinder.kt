package com.avito.instrumentation.impact

import com.avito.utils.logging.CILogger
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

private typealias FilePath = String

internal class ModifiedKotlinClassesFinder(
    private val projectDir: File,
    private val logger: CILogger
) {

    private val psiProject: Project = createKotlinCoreEnvironment()

    private val compiler: KotlinCompiler = KotlinCompiler(project = psiProject)

    /**
     * @param path relative project file path
     */
    fun find(path: FilePath): List<Regex> {
        val file = File(projectDir, path)

        return if (file.exists() && file.canRead() && file.extension == KOTLIN_FILE_EXTENSION) {

            logger.info("Parsing AST from: ${file.path}")

            val parsedKotlinFile = compiler.compile(file)
            val packagePath = parsedKotlinFile.packageFqName.asString()
                .replace(".", "/")

            if (packagePath.isNotEmpty()) {
                val fileNameBasedRegex = getRegexBasedOnFileName(
                    fileName = file.nameWithoutExtension,
                    packagePath = packagePath
                )
                val contentBasedRegex = getRegexBasedOnFileContent(
                    compiledFile = parsedKotlinFile,
                    packagePath = packagePath
                )

                contentBasedRegex.plus(fileNameBasedRegex)
            } else {
                logger.info("Couldn't find package id inside ${file.path}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Get all possible classes paths inside bytecode by file name
     */
    private fun getRegexBasedOnFileName(
        fileName: String,
        packagePath: String
    ): Regex = "$packagePath/$fileName(Kt\\.class)".toRegex()

    /**
     * Get all possible classes paths inside bytecode by source file content
     */
    private fun getRegexBasedOnFileContent(
        compiledFile: KtFile,
        packagePath: String
    ): List<Regex> =
        compiledFile.declarations
            .asSequence()
            .filterIsInstance<KtClassOrObject>()
            .map { it.name }
            .map { "$packagePath/$it(\\.class|$.*\\.class)" }
            .map { it.toRegex() }
            .toList()

    companion object {

        private const val KOTLIN_FILE_EXTENSION = "kt"

        internal fun createKotlinCoreEnvironment(): Project {
            // Based on detekt by Artur Bosch
            val configuration = CompilerConfiguration()

            configuration.put(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false)
            )

            return KotlinCoreEnvironment.createForProduction(
                parentDisposable = Disposer.newDisposable(),
                configuration = configuration,
                configFiles = EnvironmentConfigFiles.JVM_CONFIG_FILES
            ).project
        }
    }
}
