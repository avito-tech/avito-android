package com.avito.android.network_contracts.validation

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedString
import com.avito.android.network_contracts.analytics.trackValidationFailed
import com.avito.android.network_contracts.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.network_contracts.validation.analyzer.NetworkContractsProblemsAnalyzer
import com.avito.android.network_contracts.validation.analyzer.diagnostic.NetworkContractsDiagnostic
import com.avito.android.network_contracts.validation.analyzer.rules.EmptyCodegenTomlFileDiagnosticRule
import com.avito.android.network_contracts.validation.analyzer.rules.EmptySchemesDiagnosticRule
import com.avito.android.network_contracts.validation.analyzer.rules.NetworkContractsDiagnosticRule
import com.avito.android.network_contracts.validation.analyzer.rules.RemoteCompatibilityDiagnosticRule
import com.avito.android.network_contracts.validation.analyzer.rules.configurations.EmptyCodegenTomlRuleConfiguration
import com.avito.android.network_contracts.validation.analyzer.rules.configurations.EmptySchemesRuleConfiguration
import com.avito.android.network_contracts.validation.analyzer.rules.configurations.RemoteCompatibilityRuleConfiguration
import com.avito.android.network_contracts.validation.analyzer.rules.configurations.RuleConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import kotlin.reflect.KClass

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
    internal val ruleConfigurations: ExtensiblePolymorphicDomainObjectContainer<RuleConfiguration> =
        objects.polymorphicDomainObjectContainer(RuleConfiguration::class.java)
            .apply {
                registerFactory(RemoteCompatibilityRuleConfiguration::class.java) {
                    objects.newInstance(RemoteCompatibilityRuleConfiguration::class.java, it)
                }
                registerFactory(EmptySchemesRuleConfiguration::class.java) {
                    objects.newInstance(EmptySchemesRuleConfiguration::class.java, it)
                }
                registerFactory(EmptyCodegenTomlRuleConfiguration::class.java) {
                    objects.newInstance(EmptyCodegenTomlRuleConfiguration::class.java, it)
                }
            }

    @TaskAction
    public fun validate() {
        val dependentVerdictsSucceed = requiredVerdicts.files.all { it.readText() == "OK" }
        if (!dependentVerdictsSucceed) {
            error("Required verdicts are not OK")
        }

        val networkContractsProblemsAnalyzer = NetworkContractsProblemsAnalyzer(createRules())

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

    public fun <T : RuleConfiguration> registerRule(name: String, configuration: KClass<T>, action: T.() -> Unit) {
        ruleConfigurations.register(name, configuration.java, action)
    }

    private fun createRules(): List<NetworkContractsDiagnosticRule> {
        return ruleConfigurations
            .map { configuration ->
                when (configuration) {
                    is EmptyCodegenTomlRuleConfiguration -> EmptyCodegenTomlFileDiagnosticRule(configuration)
                    is EmptySchemesRuleConfiguration -> EmptySchemesDiagnosticRule(configuration)
                    is RemoteCompatibilityRuleConfiguration -> RemoteCompatibilityDiagnosticRule(configuration)
                    else -> error("Unknown configuration type: $configuration")
                }
            }
    }

    internal companion object {

        internal const val NAME = "validateNetworkContracts"
        private const val OK = "OK"
    }
}

public inline fun <reified T : RuleConfiguration> ValidateNetworkContractsTask.registerRule(
    name: String,
    noinline action: T.() -> Unit
) {
    registerRule(name, T::class, action)
}
