package com.avito.android.network_contracts.validation

import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsIssue

internal object ProblemsMessageBuilder {

    fun build(diagnostics: Map<NetworkContractsIssue, List<NetworkContractsDiagnostic>>): String {
        return buildString {
            appendLine("Validation of the network contracts plugin failed:")

            diagnostics.forEach { (issue, diagnosticsList) ->
                appendLine("- ${issue.title}:")
                diagnosticsList.forEach { diagnostic ->
                    appendLine(" - ${diagnostic.message}")
                }
            }

            appendLine()
            appendLine("You can locally run validation task:")
            appendLine("`./gradlew ${ValidateNetworkContractsTask.NAME}`")
        }
    }
}
