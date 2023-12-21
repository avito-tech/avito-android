package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.validation.analyzer.rules.CorruptedFilesDiagnosticRule
import com.avito.android.network_contracts.validation.analyzer.rules.NetworkContractsDiagnosticRule
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

public abstract class ValidateNetworkContractsGeneratedFilesTask : ValidateNetworkContractsTask() {

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

    override fun createRules(): List<NetworkContractsDiagnosticRule> {
        return listOf(
            CorruptedFilesDiagnosticRule(
                generatedFilesDir = generatedFilesDirectory.get().asFile,
                referencesFilesDir = referenceFilesDirectory.get().asFile,
            ),
        )
    }

    internal companion object {

        internal const val NAME = "validateGeneratedFilesNetworkContracts"
    }
}
