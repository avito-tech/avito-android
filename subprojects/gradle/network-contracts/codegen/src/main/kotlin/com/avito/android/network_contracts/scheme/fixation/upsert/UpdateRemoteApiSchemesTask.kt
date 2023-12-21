package com.avito.android.network_contracts.scheme.fixation.upsert

import com.avito.android.network_contracts.internal.http.HttpClientService
import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.scheme.fixation.upsert.data.UpdateApiSchemesService
import com.avito.android.network_contracts.scheme.fixation.upsert.data.UpdateApiSchemesServiceImpl
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class UpdateRemoteApiSchemesTask : DefaultTask() {

    @get:Input
    public abstract val author: Property<String>

    @get:Input
    public abstract val branchName: Property<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val schemes: ConfigurableFileCollection

    @get:InputFile
    public abstract val validationReport: RegularFileProperty

    @get:Internal
    internal abstract val httpClientService: Property<HttpClientService>

    @get:Internal
    internal abstract val loggerFactory: Property<LoggerFactory>

    private val logger: Logger by lazy { loggerFactory.get().create(NAME) }

    @TaskAction
    public fun upsert() {
        val validationResult = validationReport.get().asFile.readText()

        if (validationResult != "OK") {
            error("Validation schemes failed.")
        }

        val schemes = schemes.filter(File::exists)
        if (schemes.isEmpty) {
            logger.warn("Schemes not found")
            return
        }

        val httpClient = httpClientService.get().buildClient()
        val service: UpdateApiSchemesService = UpdateApiSchemesServiceImpl(httpClient)

        val apiSchemes = schemes
            .map { Json.decodeFromStream<ApiSchemesMetadata>(it.inputStream()) }

        runBlocking {
            service.sendContracts(
                author = author.get(),
                version = extractSchemesVersion(),
                schemes = apiSchemes,
            )
        }
    }

    /**
     * Extract scheme version from git branch name:
     *  * develop -> develop
     *  * release-avito/165.0 -> 165.0
     */
    private fun extractSchemesVersion(): String {
        return branchName.get().split("/").last()
    }

    internal companion object {

        internal const val NAME = "updateRemoteApiSchemes"
    }
}
