package com.avito.android.contracts.platform.scheme.validation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.contracts.platform.analytics.trackValidationFailed
import com.avito.android.contracts.platform.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.contracts.platform.scheme.validation.analyzer.NetworkContractsProblemsAnalyzer
import com.avito.android.contracts.platform.scheme.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.contracts.platform.scheme.validation.analyzer.rules.NetworkContractsDiagnosticRule
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public abstract class ValidateNetworkContractsTask @Inject constructor(
    private val objects: ObjectFactory
) : DefaultTask(), BuildVerdictTask {

    @get:Input
    public abstract val modulePath: Property<String>

    @get:InputFiles
    @get:Optional
    public abstract val requiredVerdicts: ConfigurableFileCollection

    @get:OutputFile
    public abstract val verdictFile: RegularFileProperty

    @get:Internal
    override val verdict: SpannedString
        get() = SpannedString(verdictFile.get().asFile.readText())

    @get:Internal
    internal abstract val analyticsTrackerService: Property<NetworkContractsAnalyticsService>

    @get:Nested
    internal abstract val rules: ListProperty<NetworkContractsDiagnosticRule>

    @TaskAction
    public fun validate() {
        val dependentVerdictsSucceed = requiredVerdicts.files.all { it.readText() == "OK" }
        if (!dependentVerdictsSucceed) {
            error("Required verdicts are not OK")
        }

        val networkContractsProblemsAnalyzer = NetworkContractsProblemsAnalyzer(rules.get())

        val validationDetections = networkContractsProblemsAnalyzer.analyze()
        var verdict = OK
        if (validationDetections.isNotEmpty()) {
            val tracker = analyticsTrackerService.get().tracker
            validationDetections.forEach { diagnostic ->
                val type = when (diagnostic) {
                    is NetworkContractsDiagnostic.Local -> "local"
                    is NetworkContractsDiagnostic.Remote -> "remote"
                    is NetworkContractsDiagnostic.Undefined -> "undefined"
                }
                tracker.trackValidationFailed(modulePath.get(), diagnostic.message, type)
            }
            val diagnostics = validationDetections.groupBy { diagnostic -> diagnostic.issue }

            verdict = ProblemsMessageBuilder.build(diagnostics)
            verdictFile.get().asFile.writeText(verdict)
            error(verdict)
        }
        verdictFile.get().asFile.writeText(verdict)
    }

    internal companion object {

        internal const val NAME = "validateNetworkContracts"
        private const val OK = "OK"
    }
}
