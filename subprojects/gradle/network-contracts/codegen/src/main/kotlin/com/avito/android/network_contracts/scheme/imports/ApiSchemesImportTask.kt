package com.avito.android.network_contracts.scheme.imports

import com.avito.android.network_contracts.internal.http.HttpClientBuilder
import com.avito.android.network_contracts.scheme.imports.data.ApiSchemesImportServiceImpl
import com.avito.android.network_contracts.scheme.imports.data.models.areSchemesExist
import com.avito.android.network_contracts.scheme.imports.data.models.entriesList
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class ApiSchemesImportTask : DefaultTask() {

    @get:Input
    public abstract val apiPath: Property<String>

    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @get:Internal
    internal abstract val httpClientBuilder: Property<HttpClientBuilder>

    @get:Internal
    internal abstract val loggerFactory: Property<LoggerFactory>

    private val logger: Logger by lazy { loggerFactory.get().create("ApiSchemesImportTask") }

    @TaskAction
    public fun fetch() {
        if (!apiPath.isPresent || apiPath.get().isEmpty()) {
            error("Unable to import api schemes as apiPath is not defined. " +
                "Please, provide url by parameter `apiSchemesUrl`")
        }

        val apiImportService = ApiSchemesImportServiceImpl(httpClientBuilder.get().buildClient())

        val rootDirectory = outputDirectory.asFile.get()
        val schemaFilesGenerator = ApiSchemesFilesGenerator(rootDirectory)

        val schema = runBlocking {
            val schemes = apiImportService.importScheme(apiPath.get()).result
            if (!schemes.areSchemesExist) {
                error("Did not find any schemes for `${apiPath.get()}`.")
            }

            schemes.entriesList()
        }

        val generatedFiles = schemaFilesGenerator.generateFiles(schema)
        logGeneratedFiles(generatedFiles)
    }

    private fun logGeneratedFiles(generatedFiles: List<File>) {
        val resultMessage = buildString {
            appendLine("Created schema files:")
            generatedFiles.forEach { file ->
                appendLine("file://${file.absolutePath}")
            }
        }
        logger.warn(resultMessage)
    }
}
