package com.avito.android.network_contracts.scheme.imports

import com.avito.android.network_contracts.internal.http.HttpClientService
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
    internal abstract val httpClientBuilder: Property<HttpClientService>

    @get:Internal
    internal abstract val loggerFactory: Property<LoggerFactory>

    private val logger: Logger by lazy { loggerFactory.get().create("ApiSchemesImportTask") }

    @TaskAction
    public fun fetch() {
        if (apiPath.get().isEmpty()) {
            error(
                "Unable to import api schemes as apiPath is not defined. " +
                    "Please, provide url by parameter `apiSchemesUrl`"
            )
        }

        val facade = ApiSchemesImportFacade.createInstance(
            httpClient = httpClientBuilder.get().buildClient(logger)
        )

        val rootDirectory = outputDirectory.get().asFile

        val generatedFiles = runBlocking {
            facade.importSchemes(apiPath.get(), rootDirectory)
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
