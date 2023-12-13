package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.validation.analyzer.NetworkContractsProblemsAnalyzer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

public abstract class ValidateNetworkContractsTask : DefaultTask() {

    /**
     * The directory where the generated contract files are located.
     *
     * The files in this directory will be checked against the reference files
     * specified in the [referenceFilesDirectory] variable.
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val generatedFilesDirectory: DirectoryProperty

    /**
     * The directory containing the reference contract files.
     *
     * The files in this directory must be the same as when the codegen command
     * was executed, without any changes made to them.
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val referenceFilesDirectory: DirectoryProperty

    @get:OutputFile
    public abstract val resultFile: RegularFileProperty

    @TaskAction
    public fun validate() {
        val networkContractsProblemsAnalyzer = NetworkContractsProblemsAnalyzer(
            generatedFilesDir = generatedFilesDirectory.get().asFile,
            referencesFilesDir = referenceFilesDirectory.get().asFile,
        )
        val validationDetections = networkContractsProblemsAnalyzer.analyze()
        resultFile.get().asFile.writeText(Json.encodeToString(validationDetections))
    }

    internal companion object {

        internal const val NAME = "validateNetworkContracts"
    }
}
