package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.validation.analyzer.rules.EmptyCodegenTomlFileDiagnosticRule
import com.avito.android.network_contracts.validation.analyzer.rules.EmptySchemesDiagnosticRule
import com.avito.android.network_contracts.validation.analyzer.rules.NetworkContractsDiagnosticRule
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

public abstract class ValidateNetworkContractsSchemesTask : ValidateNetworkContractsTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val schemes: ConfigurableFileCollection

    @get:InputFile
    @get:Optional
    public abstract val codegenTomlFilePath: RegularFileProperty

    @get:Internal
    public abstract val projectPath: Property<String>

    override fun createRules(): List<NetworkContractsDiagnosticRule> {
        return listOf(
            EmptySchemesDiagnosticRule(
                modulePath = projectPath.get(),
                schemes = schemes.asFileTree.files
            ),
            EmptyCodegenTomlFileDiagnosticRule(
                modulePath = projectPath.get(),
                codegenTomlFile = codegenTomlFilePath.asFile.orNull,
            )
        )
    }

    internal companion object {

        internal const val NAME = "validateSchemesNetworkContracts"
    }
}
