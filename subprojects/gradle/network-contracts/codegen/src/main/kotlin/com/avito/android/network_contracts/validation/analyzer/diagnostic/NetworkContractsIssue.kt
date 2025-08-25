package com.avito.android.network_contracts.validation.analyzer.diagnostic

import kotlinx.serialization.Serializable

@Serializable
internal data class NetworkContractsIssue(
    val key: String = this::class.java.name,
    val title: String = key,
)

@Serializable
internal sealed class NetworkContractsDiagnostic {

    abstract val issue: NetworkContractsIssue
    abstract val message: String

    /**
     * Local diagnostic - used for errors occurring during local checks.
     * This includes validations like searching for required files (codegen.toml, etc.)
     * and other local file system validations.
     */
    @Serializable
    internal data class Local(
        override val issue: NetworkContractsIssue,
        override val message: String,
    ) : NetworkContractsDiagnostic()

    /**
     * Remote diagnostic - used for errors from validation with api-composition-storage service.
     * This service validates that the schema is valid relative to schemas on backend services.
     */
    @Serializable
    internal data class Remote(
        override val issue: NetworkContractsIssue,
        override val message: String,
    ) : NetworkContractsDiagnostic()

    /**
     * Undefined diagnostic - used for errors that could not be precisely classified.
     */
    @Serializable
    internal data class Undefined(
        override val issue: NetworkContractsIssue,
        override val message: String,
    ) : NetworkContractsDiagnostic()
}
