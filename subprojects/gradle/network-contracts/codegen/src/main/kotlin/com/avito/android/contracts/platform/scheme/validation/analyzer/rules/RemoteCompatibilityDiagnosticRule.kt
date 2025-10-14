package com.avito.android.contracts.platform.scheme.validation.analyzer.rules

import com.avito.android.contracts.platform.analytics.trackValidationDuration
import com.avito.android.contracts.platform.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata
import com.avito.android.contracts.platform.scheme.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.contracts.platform.scheme.validation.analyzer.diagnostic.NetworkContractsIssue
import com.avito.android.contracts.platform.scheme.validation.data.ValidationApiSchemesService
import com.avito.android.contracts.platform.shared.extractSchemesVersionFromBranch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import java.io.File
import javax.inject.Inject
import kotlin.time.measureTime

public abstract class RemoteCompatibilityDiagnosticRule @Inject constructor() : NetworkContractsDiagnosticRule() {

    @get:InputFiles
    @get:Optional
    public abstract val schemes: ConfigurableFileCollection

    @get:Input
    public abstract val branchName: Property<String>

    @get:Input
    public abstract val modulePath: Property<String>

    @get:Input
    public abstract val kind: Property<String>

    @get:Input
    public abstract val variantName: Property<String>

    @get:Internal
    public abstract val validationService: Property<ValidationApiSchemesService>

    @get:Internal
    public abstract val analyticsTrackerService: Property<NetworkContractsAnalyticsService>

    override val issue: NetworkContractsIssue = NetworkContractsIssue(
        key = RemoteCompatibilityDiagnosticRule::class.java.name,
        title = "Schemes errors",
    )

    override fun analyze() {
        val schemes = schemes.files
        if (schemes.isEmpty()) {
            report(
                NetworkContractsDiagnostic.Local(
                    issue,
                    message = "Module `$modulePath` applies plugin, " +
                        "but does not contain any network contracts schemes.",
                )
            )
            return
        }

        val analyticsTracker = analyticsTrackerService.get().tracker
        val elapsedTime = measureTime { innerAnalyze(schemes) }
        analyticsTracker.trackValidationDuration(elapsedTime, modulePath.get(), kind.get(), variantName.get())
    }

    private fun innerAnalyze(schemes: Set<File>) {
        val apiSchemes = schemes
            .map { schema -> schema.inputStream().use { Json.decodeFromStream<ApiSchemesMetadata>(it) } }

        val validationService = validationService.get()

        runBlocking {
            val result = runCatching {
                validationService.validate(
                    version = extractSchemesVersionFromBranch(branchName.get()),
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
