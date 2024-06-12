package com.avito.android.network_contracts.validation.analyzer.rules

import java.io.File

internal class EmptyCodegenTomlFileDiagnosticRule(
    private val codegenTomlFile: File?,
    private val modulePath: String
) : NetworkContractsDiagnosticRule() {

    override fun analyze() {
        if (codegenTomlFile == null) {
            report("codegen.toml file is omitted in the `$modulePath` module. " +
                "Please, check that you have added codegen.toml file to git.")
        } else if (codegenTomlFile.length() == 0L) {
            report("codegen.toml file is empty in the `$modulePath` module. " +
                "Please, check the codegen.toml file.")
        }
    }
}
