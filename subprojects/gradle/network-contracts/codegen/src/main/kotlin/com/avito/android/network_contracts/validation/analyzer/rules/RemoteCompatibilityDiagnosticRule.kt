package com.avito.android.network_contracts.validation.analyzer.rules

import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.shared.extractSchemesVersionFromBranch
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsIssue
import com.avito.android.network_contracts.validation.analyzer.rules.configurations.RemoteCompatibilityRuleConfiguration
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

internal class RemoteCompatibilityDiagnosticRule(
    private val configuration: RemoteCompatibilityRuleConfiguration,
) : NetworkContractsDiagnosticRule() {

    override val issue: NetworkContractsIssue = NetworkContractsIssue(
        key = RemoteCompatibilityDiagnosticRule::class.java.name,
        title = "Schemes errors",
    )

    override fun analyze() {
        val schemes = configuration.schemes.files
        if (schemes.isEmpty()) {
            report("Module `$configuration.modulePath` applies plugin, " +
                "but does not contain any network contracts schemes.")
            return
        }

        val apiSchemes = schemes
            .map { schema -> schema.inputStream().use { Json.decodeFromStream<ApiSchemesMetadata>(it) } }

        val validationService = configuration.validationService.get()

        runBlocking {
            val result = runCatching {
                validationService.validate(
                    version = extractSchemesVersionFromBranch(configuration.branchName.get()),
                    schemes = apiSchemes,
                )
            }

            result.fold(
                onSuccess = { errors ->
                    errors.forEach { error -> report(error.message) }
                },
                onFailure = {
                    report("Validation failed with error: ${it.message}")
                }
            )
        }
    }
}
