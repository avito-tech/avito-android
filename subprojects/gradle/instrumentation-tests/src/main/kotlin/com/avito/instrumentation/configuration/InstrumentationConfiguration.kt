package com.avito.instrumentation.configuration

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.reservation.request.Device
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

    var impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off

    var kubernetesNamespace = "android-emulator"

    var enableDeviceDebug: Boolean = false

    var filter = "default"

    abstract val targetsContainer: NamedDomainObjectContainer<TargetConfiguration>

    val targets: List<TargetConfiguration>
        get() = targetsContainer.toList()
            .filter { it.enabled }

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
            reportSkippedTests = reportSkippedTests,
            impactAnalysisPolicy = impactAnalysisPolicy,
            reportFlakyTests = reportFlakyTests,
            kubernetesNamespace = kubernetesNamespace,
            targets = targets.map {
                it.data(parentInstrumentationParameters = mergedInstrumentationParameters)
            },
            performanceType = performanceType,
            enableDeviceDebug = enableDeviceDebug,
            filter = filters.singleOrNull { it.name == filter } ?: throw IllegalStateException("Can't find filter=$filter")
        )
    }

    data class Data(
        val name: String,
        val instrumentationParams: InstrumentationParameters,
        val tryToReRunOnTargetBranch: Boolean,
        val reportFlakyTests: Boolean,
        val reportSkippedTests: Boolean,
        val impactAnalysisPolicy: ImpactAnalysisPolicy,
        val kubernetesNamespace: String,
        val targets: List<TargetConfiguration.Data>,
        val performanceType: PerformanceType?,
        val enableDeviceDebug: Boolean,
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
            "$name, targets: $targets, filter: $filter "

        companion object
    }
}
