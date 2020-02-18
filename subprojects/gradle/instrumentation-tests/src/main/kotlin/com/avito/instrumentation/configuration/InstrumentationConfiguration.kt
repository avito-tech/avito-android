package com.avito.instrumentation.configuration

import com.avito.instrumentation.configuration.target.TargetConfiguration
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import java.io.Serializable

@Suppress("MemberVisibilityCanBePrivate")
open class InstrumentationConfiguration(val name: String) {

    var suppressGroups: List<String> = emptyList()

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
     * При последующих запусках на том же коммите и на той же ветке запускать только упавшие билды с прошлого запуска
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

    lateinit var targetsContainer: NamedDomainObjectContainer<TargetConfiguration>
    val targets: List<TargetConfiguration>
        get() = targetsContainer.toList()
            .filter { it.enabled }

    fun targets(closure: Closure<NamedDomainObjectContainer<TargetConfiguration>>) {
        targetsContainer.configure(closure)
    }

    fun validate() {
        targets.forEach { it.validate() }
    }

    fun data(parentInstrumentationParameters: InstrumentationParameters): Data {
        require(kubernetesNamespace.isNotBlank())

        val mergedInstrumentationParameters: InstrumentationParameters = parentInstrumentationParameters
            .applyParameters(instrumentationParams)

        return Data(
            name = name,
            suppressGroups = suppressGroups,
            instrumentationParams = mergedInstrumentationParameters,
            tryToReRunOnTargetBranch = tryToReRunOnTargetBranch,
            rerunFailedTests = rerunFailedTests,
            reportSkippedTests = reportSkippedTests,
            annotatedWith = annotatedWith,
            tests = tests,
            impactAnalysisPolicy = impactAnalysisPolicy,
            reportFlakyTests = reportFlakyTests,
            prefixFilter = prefixFilter,
            kubernetesNamespace = kubernetesNamespace,
            targets = targets.map {
                it.data(parentInstrumentationParameters = mergedInstrumentationParameters)
            },
            performanceType = performanceType
        )
    }

    data class Data(
        val name: String,
        val suppressGroups: List<String>,
        val instrumentationParams: InstrumentationParameters,
        val tryToReRunOnTargetBranch: Boolean,
        val reportFlakyTests: Boolean,
        val rerunFailedTests: Boolean,
        val reportSkippedTests: Boolean,
        val impactAnalysisPolicy: ImpactAnalysisPolicy,
        val annotatedWith: Collection<String>?,
        val tests: List<String>?,
        val prefixFilter: String?,
        val kubernetesNamespace: String,
        val targets: List<TargetConfiguration.Data>,
        val performanceType: PerformanceType?
    ) : Serializable {

        override fun toString(): String = "$name with targets: $targets for tests annotated with: $annotatedWith"

        companion object
    }
}
