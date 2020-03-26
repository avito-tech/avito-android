package com.avito.instrumentation.configuration

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.reservation.request.Device
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import java.io.Serializable

@Suppress("MemberVisibilityCanBePrivate")
open class InstrumentationConfiguration(val name: String) {

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

    /**
     * Applied only in next instrumentation invoke at the same git HEAD
     * It remove already succeed test from execution
     */
    var rerunFailedTests = true

    var reportFlakyTests = false

    /**
     * Отправлять в репорт ignored/skipped тесты, нужно для проверок тестов на ПР c потенцианльно полным сьютом тестов
     * не нужно для проверок типа performance
     */
    var reportSkippedTests = false

    var annotatedWith: Collection<String>? = null

    var impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off

    var tests: List<String>? = null

    var prefixFilter: String? = null

    var kubernetesNamespace = "android-emulator"

    /**
     * It must be a valid reportId.
     * Get test runs from report by id. Filter already succeed or new tests.
     * Stay only failed contained in report
     */
    var filterSucceedAndNewTestsByReport: String? = null

    lateinit var targetsContainer: NamedDomainObjectContainer<TargetConfiguration>

    val targets: List<TargetConfiguration>
        get() = targetsContainer.toList()
            .filter { it.enabled }

    fun targets(closure: Closure<NamedDomainObjectContainer<TargetConfiguration>>) {
        targetsContainer.configure(closure)
    }

    fun validate() {
        require(kubernetesNamespace.isNotBlank()) { "kubernetesNamespace must be set" }
        targets.forEach { it.validate() }
    }

    fun data(parentInstrumentationParameters: InstrumentationParameters): Data {

        val mergedInstrumentationParameters: InstrumentationParameters = parentInstrumentationParameters
            .applyParameters(instrumentationParams)

        return Data(
            name = name,
            instrumentationParams = mergedInstrumentationParameters,
            tryToReRunOnTargetBranch = tryToReRunOnTargetBranch,
            filterSucceedTestsByPreviousRun = rerunFailedTests,
            reportSkippedTests = reportSkippedTests,
            annotatedWith = annotatedWith,
            filterTestsByName = tests,
            impactAnalysisPolicy = impactAnalysisPolicy,
            reportFlakyTests = reportFlakyTests,
            prefixFilter = prefixFilter,
            kubernetesNamespace = kubernetesNamespace,
            targets = targets.map {
                it.data(parentInstrumentationParameters = mergedInstrumentationParameters)
            },
            performanceType = performanceType,
            filterSucceedAndNewByReport = filterSucceedAndNewTestsByReport
        )
    }

    data class Data(
        val name: String,
        val instrumentationParams: InstrumentationParameters,
        val tryToReRunOnTargetBranch: Boolean,
        val reportFlakyTests: Boolean,
        val filterSucceedTestsByPreviousRun: Boolean,
        val reportSkippedTests: Boolean,
        val impactAnalysisPolicy: ImpactAnalysisPolicy,
        val annotatedWith: Collection<String>?,
        val filterTestsByName: List<String>?,
        val prefixFilter: String?,
        val kubernetesNamespace: String,
        val targets: List<TargetConfiguration.Data>,
        val performanceType: PerformanceType?,
        val filterSucceedAndNewByReport: String?
    ) : Serializable {

        init {
            val hasLocal = targets.any { it.reservation.device is Device.LocalEmulator }
            val hasKubernetes = targets.any { it.reservation.device is Device.Emulator }
            if (hasLocal && hasKubernetes) {
                throw IllegalStateException("Targeting to local and kubernetes emulators at the same configuration $name is not supported yet")
            }
        }

        override fun toString(): String = "$name with targets: $targets for tests annotated with: $annotatedWith"

        companion object
    }
}
