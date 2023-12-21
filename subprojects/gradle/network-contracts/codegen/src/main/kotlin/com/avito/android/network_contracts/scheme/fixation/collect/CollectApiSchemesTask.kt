package com.avito.android.network_contracts.scheme.fixation.collect

import com.avito.android.network_contracts.scheme.fixation.collect.collector.ApiSchemesCollector
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.Base64

public abstract class CollectApiSchemesTask : DefaultTask() {

    @get:Input
    public abstract val projectPath: Property<String>

    @get:Input
    public abstract val projectName: Property<String>

    @get:InputDirectory
    public abstract val schemesDirectory: DirectoryProperty

    @get:InputFile
    public abstract val codegenTomlFile: RegularFileProperty

    @get:OutputFile
    public abstract val jsonSchemeMetadataFile: RegularFileProperty

    @TaskAction
    public fun upsert() {
        val schemesCollector = ApiSchemesCollector(projectPath.get())

        val schemesFile = schemesCollector.collect(codegenTomlFile.get().asFile, schemesDirectory.get().asFile)
        val contracts = schemesFile.mapValues { Base64.getEncoder().encodeToString(it.value.readBytes()) }
        val apiSchemesMetadata = ApiSchemesMetadata(
            projectName = projectName.get(),
            schemes = contracts
        )

        jsonSchemeMetadataFile.get().asFile.writeText(Json.encodeToString(apiSchemesMetadata))
    }

    internal companion object {

        const val NAME = "collectApiSchemes"
    }
}
