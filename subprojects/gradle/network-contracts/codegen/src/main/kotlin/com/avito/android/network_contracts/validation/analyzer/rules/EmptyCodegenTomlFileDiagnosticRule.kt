package com.avito.android.network_contracts.validation.analyzer.rules

import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.network_contracts.validation.analyzer.rules.configurations.EmptyCodegenTomlRuleConfiguration

internal class EmptyCodegenTomlFileDiagnosticRule(
    private val configuration: EmptyCodegenTomlRuleConfiguration,
) : NetworkContractsDiagnosticRule() {

    override fun analyze() {
        val modulePath = configuration.modulePath.get()
        val codegenTomlFile = configuration.codegenTomlFile.asFile.orNull

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
