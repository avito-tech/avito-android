package com.avito.android.contracts.platform.scheme.imports

import com.avito.android.contracts.platform.scheme.imports.data.SchemesImportService
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class ApiSchemesImportTask : DefaultTask() {

    @get:Input
    public abstract val apiPath: Property<String>

    @get:Input
    @get:Optional
    public abstract val gateway: Property<String>

    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @get:Internal
    internal abstract val importService: Property<SchemesImportService<*>>

    @get:Internal
    internal abstract val loggerFactory: Property<LoggerFactory>

    private val logger: Logger by lazy { loggerFactory.get().create("ApiSchemesImportTask") }

    @TaskAction
    public fun fetch() {
        if (apiPath.get().isEmpty()) {
            error(
                "Unable to import api schemes as apiPath is not defined. " +
                    "Please, provide url by parameter `avito.networkContracts.schemesPath`"
            )
        }

        val facade = ApiSchemesImportFacade(importService.get())

        val rootDirectory = outputDirectory.get().asFile

        val generatedFiles = runBlocking {
            facade.importSchemes(gateway.orNull.orEmpty(), apiPath.get(), rootDirectory)
        }

        logGeneratedFiles(generatedFiles)
    }

    private fun logGeneratedFiles(generatedFiles: List<File>) {
        val resultMessage = buildString {
            appendLine("Created schema files:")
            generatedFiles.forEach { file ->
                appendLine("file://${file.absolutePath}")
            }
        }
        logger.info(resultMessage)
    }

    public companion object {

        internal const val NAME: String = "addEndpoint"
    }
}
