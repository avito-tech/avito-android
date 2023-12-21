package com.avito.android.network_contracts.validation.analyzer.diagnostic

import kotlinx.serialization.Serializable

@Serializable
internal data class NetworkContractsIssue(
    val key: String = this::class.java.name,
)

@Serializable
internal sealed class NetworkContractsDiagnostic {
    abstract val issue: NetworkContractsIssue
    abstract val message: String
}

@Serializable
internal data class DefaultNetworkContractsDiagnostic(
    override val issue: NetworkContractsIssue,
    override val message: String,
) : NetworkContractsDiagnostic()
