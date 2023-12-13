package com.avito.android.network_contracts.validation.diagnostic

import kotlinx.serialization.Serializable

@Serializable
internal sealed class NetworkContractsDiagnostic {

    @Serializable
    data class Failure(
        val corruptedFilePaths: List<String>
    ) : NetworkContractsDiagnostic()
}
