package com.avito.android.network_contracts.validation.analyzer.rules

import com.avito.android.network_contracts.analytics.trackValidationDuration
import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.shared.extractSchemesVersionFromBranch
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsIssue
import com.avito.android.network_contracts.validation.analyzer.rules.configurations.RemoteCompatibilityRuleConfiguration
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import kotlin.time.measureTime

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
            report(
                NetworkContractsDiagnostic.Local(
                    issue,
                    message = "Module `$configuration.modulePath` applies plugin, " +
                        "but does not contain any network contracts schemes.",
                )
            )
            return
        }

        val analyticsTracker = configuration.analyticsTrackerService.get().tracker
        val elapsedTime = measureTime { innerAnalyze(schemes) }
        analyticsTracker.trackValidationDuration(elapsedTime, configuration.modulePath.get())
    }

    private fun innerAnalyze(schemes: Set<File>) {
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
                    errors.forEach { error ->
                        report(NetworkContractsDiagnostic.Remote(issue, error.message))
                    }
                },
                onFailure = {
                    report(
                        NetworkContractsDiagnostic.Undefined(
                            issue = issue,
                            message = "Validation failed with error: ${it.message}",
                        )
                    )
                }
            )
        }
    }
}
