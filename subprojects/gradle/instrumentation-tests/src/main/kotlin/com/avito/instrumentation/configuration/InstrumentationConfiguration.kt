package com.avito.instrumentation.configuration

import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.suite.filter.Filter
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import java.io.Serializable

@Suppress("MemberVisibilityCanBePrivate")
abstract class InstrumentationConfiguration(val name: String) {

    var instrumentationParams: Map<String, String> = emptyMap()

    /**
     * определяем нужно ли к конфигурации применять особую логику для измерения performance во время тестов
     *
     * TODO разделить конфигурации MBS-6926
     */
    var performanceType: PerformanceType? = null

    enum class PerformanceType { SIMPLE, MDE }

    /**
     * Если тесты на ветке в упали, сразу после пытаемся прогнать эти упавшие тесты на сборке из таргетной ветки (в которую пытаемся смерджиться)
     * И если там тоже все плохо - списываем все на инфраструктурные проблемы и позволяем смерджить пулл-реквест
     *
     * Логика для принятия решения о типе падений в [com.avito.instrumentation.rerun.MergeResultsWithTargetBranchRun]
     */
    var tryToReRunOnTargetBranch = false


    var reportFlakyTests = false

    /**
     * Отправлять в репорт ignored/skipped тесты, нужно для проверок тестов на ПР c потенцианльно полным сьютом тестов
     * не нужно для проверок типа performance
     */
    var reportSkippedTests = false

    /**
     * Applied only in next instrumentation invoke at the same git HEAD
     * It remove already succeed test from execution
     */
    @Deprecated("since 2020.3.6")
    var rerunFailedTests = true

    @Deprecated("since 2020.3.6")
    var annotatedWith: Collection<String>? = null

    @Deprecated("since 2020.3.6")
    var tests: List<String>? = null

    @Deprecated("since 2020.3.6")
    var prefixFilter: String? = null

    /**
     * It must be a valid reportId.
     * Get test runs from report by id. Filter already succeed or new tests.
     * Stay only failed contained in report
     */
    @Deprecated("since 2020.3.6")
    var keepFailedTestsFromReport: String? = null

    var impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off

    var kubernetesNamespace = "android-emulator"

    var enableDeviceDebug: Boolean = false

    var filter = "default"

    abstract val targetsContainer: NamedDomainObjectContainer<TargetConfiguration>

    val targets: List<TargetConfiguration>
        get() = targetsContainer.toList()
            .filter { it.enabled }

    @Deprecated("since 2020.3.6 https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:interoperability", replaceWith = ReplaceWith("targets(Action<NamedDomainObjectContainer<TargetConfiguration>>)"))
    fun targets(closure: Closure<NamedDomainObjectContainer<TargetConfiguration>>) {
        targetsContainer.configure(closure)
    }

    fun targets(action: Action<NamedDomainObjectContainer<TargetConfiguration>>) {
        action.execute(targetsContainer)
    }

    fun validate() {
        require(kubernetesNamespace.isNotBlank()) { "kubernetesNamespace must be set" }
        targets.forEach { it.validate() }
    }

    fun data(
        parentInstrumentationParameters: InstrumentationParameters,
        filters: List<InstrumentationFilter.Data>
    ): Data {

        val mergedInstrumentationParameters: InstrumentationParameters =
            parentInstrumentationParameters
                .applyParameters(instrumentationParams)

        return Data(
            name = name,
            instrumentationParams = mergedInstrumentationParameters,
            tryToReRunOnTargetBranch = tryToReRunOnTargetBranch,
            skipSucceedTestsFromPreviousRun = rerunFailedTests,
            reportSkippedTests = reportSkippedTests,
            keepTestsAnnotatedWith = annotatedWith,
            keepTestsWithNames = tests,
            impactAnalysisPolicy = impactAnalysisPolicy,
            reportFlakyTests = reportFlakyTests,
            keepTestsWithPrefix = prefixFilter,
            kubernetesNamespace = kubernetesNamespace,
            targets = targets.map {
                it.data(parentInstrumentationParameters = mergedInstrumentationParameters)
            },
            performanceType = performanceType,
            enableDeviceDebug = enableDeviceDebug,
            keepFailedTestsFromReport = keepFailedTestsFromReport,
            filter = filters.singleOrNull { it.name == filter }
                ?: createBackwardCompatibilityFilter())
    }

    @Deprecated(
        "since 2020.3.6",
        replaceWith = ReplaceWith("throw IllegalStateException(\"can't find filter by name\")")
    )
    private fun createBackwardCompatibilityFilter(): InstrumentationFilter.Data {
        val includedPrefixes = mutableSetOf<String>()
        tests?.let { includedPrefixes.addAll(it) }
        prefixFilter?.let { includedPrefixes.add(it) }
        return InstrumentationFilter.Data(
            name = "syntheticFilter$name",
            fromSource = InstrumentationFilter.Data.FromSource(
                prefixes = Filter.Value(
                    included = includedPrefixes.toSet(),
                    excluded = setOf()
                ),
                annotations = Filter.Value(
                    included = annotatedWith?.toSet() ?: emptySet(),
                    excluded = setOf()
                )
            ),
            fromRunHistory = InstrumentationFilter.Data.FromRunHistory(
                previousStatuses = Filter.Value(
                    included = setOf(),
                    excluded = if (rerunFailedTests) {
                        setOf(RunStatus.Success, RunStatus.Manual)
                    } else emptySet()
                ),
                reportFilter = keepFailedTestsFromReport?.let { id ->
                    InstrumentationFilter.Data.FromRunHistory.ReportFilter(
                        id = id,
                        statuses = Filter.Value(
                            included = setOf(RunStatus.Failed),
                            excluded = setOf()
                        )
                    )
                }
            )
        )

    }

    data class Data(
        val name: String,
        val instrumentationParams: InstrumentationParameters,
        val tryToReRunOnTargetBranch: Boolean,
        val reportFlakyTests: Boolean,
        val skipSucceedTestsFromPreviousRun: Boolean,
        val reportSkippedTests: Boolean,
        val impactAnalysisPolicy: ImpactAnalysisPolicy,
        val keepTestsAnnotatedWith: Collection<String>?,
        val keepTestsWithNames: List<String>?,
        val keepTestsWithPrefix: String?,
        val kubernetesNamespace: String,
        val targets: List<TargetConfiguration.Data>,
        val performanceType: PerformanceType?,
        val enableDeviceDebug: Boolean,
        val keepFailedTestsFromReport: String?,
        val filter: InstrumentationFilter.Data
    ) : Serializable {

        init {
            val hasLocal = targets.any { it.reservation.device is Device.LocalEmulator }
            val hasKubernetes = targets.any { it.reservation.device is Device.Emulator }
            if (hasLocal && hasKubernetes) {
                throw IllegalStateException("Targeting to local and kubernetes emulators at the same configuration $name is not supported yet")
            }
        }

        override fun toString(): String =
            "$name with targets: $targets for tests annotated with: $keepTestsAnnotatedWith"

        companion object
    }
}
