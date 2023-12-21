package com.avito.android.network_contracts.validation.analyzer.rules

import com.avito.android.network_contracts.validation.analyzer.diagnostic.DefaultNetworkContractsDiagnostic
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsIssue

internal abstract class NetworkContractsDiagnosticRule {

    open val issue: NetworkContractsIssue = NetworkContractsIssue(
        key = javaClass.simpleName
    )

    val findings: List<NetworkContractsDiagnostic>
        get() = internalFindings.toList()

    private val internalFindings: MutableList<NetworkContractsDiagnostic> = mutableListOf()

    abstract fun analyze()

    protected fun report(message: String) {
        internalFindings.add(DefaultNetworkContractsDiagnostic(issue, message))
    }

    protected fun report(diagnostic: NetworkContractsDiagnostic) {
        internalFindings.add(diagnostic)
    }
}
