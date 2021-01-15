package com.avito.instrumentation.impact

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.parentOrNull
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import java.io.File

class KotlinClassesFinderImpl : KotlinClassesFinder {

    private val psiProject: Project = createKotlinCoreEnvironment()

    private val compiler: KotlinCompiler = KotlinCompiler(project = psiProject)

    override fun findClasses(file: File): Sequence<FullClassName> {
        require(file.exists() && file.canRead()) {
            "Can't access file: ${file.path}"
        }
        require(file.extension == KOTLIN_FILE_EXTENSION) {
            "Only .kt files are supported; provided: ${file.path}"
        }

        val parsedKotlinFile = compiler.compile(file)

        return parsedKotlinFile.declarations
            .asSequence()
            .filterIsInstance<KtClassOrObject>()
            .map { it.fqName?.toFullName() }
            .filterNotNull()
            .ifEmpty { getJavaFileFacadeName(parsedKotlinFile) }
    }

    /**
     * There is a special case when kotlin file without any classes in it, but with declared functions or variables
     * compiles to generated <FileNameKt> class
     *
     * Only declared functions case handled here; we don't need anything else right now
     */
    private fun getJavaFileFacadeName(parsedKotlinFile: KtFile): Sequence<FullClassName> {
        return if (parsedKotlinFile.declarations.filterIsInstance<KtFunction>().isNotEmpty()) {
            sequenceOf(parsedKotlinFile.javaFileFacadeFqName.toFullName())
        } else {
            emptySequence()
        }
    }

    private fun FqName.toFullName(): FullClassName =
        FullClassName(parentOrNull()?.asString() ?: "", shortName().asString())

    companion object {

        const val KOTLIN_FILE_EXTENSION = "kt"

        /**
         *  Based on detekt by Artur Bosch
         *  todo internal
         */
        fun createKotlinCoreEnvironment(): Project {
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
