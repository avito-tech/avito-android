package com.avito.android.contracts.platform.scheme.validation.analyzer

import com.avito.android.contracts.platform.scheme.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.contracts.platform.scheme.validation.analyzer.rules.NetworkContractsDiagnosticRule

internal class NetworkContractsProblemsAnalyzer(
    private val rules: List<NetworkContractsDiagnosticRule>
) {

    fun analyze(): List<NetworkContractsDiagnostic> {
        val detections = rules.flatMap {
            it.analyze()
            it.findings
        }
        return detections
    }

    companion object {

        fun create(vararg rules: NetworkContractsDiagnosticRule): NetworkContractsProblemsAnalyzer {
            return NetworkContractsProblemsAnalyzer(rules.toList())
        }
    }
}
