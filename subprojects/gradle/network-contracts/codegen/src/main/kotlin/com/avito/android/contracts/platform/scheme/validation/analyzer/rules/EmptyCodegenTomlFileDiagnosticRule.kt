package com.avito.android.contracts.platform.scheme.validation.analyzer.rules

import com.avito.android.contracts.platform.scheme.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import javax.inject.Inject

public abstract class EmptyCodegenTomlFileDiagnosticRule @Inject constructor() : NetworkContractsDiagnosticRule() {

    @get:Optional
    @get:InputFile
    public abstract val codegenTomlFile: RegularFileProperty

    @get:Input
    public abstract val modulePath: Property<String>

    override fun analyze() {
        val modulePath = modulePath.get()
        val codegenTomlFile = codegenTomlFile.asFile.orNull

        if (codegenTomlFile == null) {
            report(
                NetworkContractsDiagnostic.Local(
                    issue,
                    message = "codegen.toml file is omitted in the `$modulePath` module. " +
                        "Please, check that you have added codegen.toml file to git.",
                )
            )
        } else if (codegenTomlFile.length() == 0L) {
            report(
                NetworkContractsDiagnostic.Local(
                    issue,
                    message = "codegen.toml file is empty in the `$modulePath` module. " +
                        "Please, check the codegen.toml file.",
                )
            )
        }
    }
}
