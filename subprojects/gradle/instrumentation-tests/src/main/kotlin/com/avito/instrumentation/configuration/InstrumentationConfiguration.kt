package com.avito.instrumentation.configuration

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.reservation.request.Device
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import java.io.Serializable

abstract class InstrumentationConfiguration(val name: String) {

    var instrumentationParams: Map<String, String> = emptyMap()

    var reportFlakyTests = false

    /**
     * Отправлять в репорт ignored/skipped тесты, нужно для проверок тестов на ПР c потенцианльно полным сьютом тестов
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
        require(targets.isNotEmpty()) { "configuration $name must have at least one target" }
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
            reportFlakyTests = reportFlakyTests,
            reportSkippedTests = reportSkippedTests,
            impactAnalysisPolicy = impactAnalysisPolicy,
            kubernetesNamespace = kubernetesNamespace,
            targets = targets.map {
                it.data(parentInstrumentationParameters = mergedInstrumentationParameters)
            },
            enableDeviceDebug = enableDeviceDebug,
            filter = filters.singleOrNull { it.name == filter }
                ?: throw IllegalStateException("Can't find filter=$filter")
        )
    }

    data class Data(
        val name: String,
        val instrumentationParams: InstrumentationParameters,
        val reportFlakyTests: Boolean,
        val reportSkippedTests: Boolean,
        val impactAnalysisPolicy: ImpactAnalysisPolicy,
        val kubernetesNamespace: String,
        val targets: List<TargetConfiguration.Data>,
        val enableDeviceDebug: Boolean,
        val filter: InstrumentationFilter.Data
    ) : Serializable {

        val isTargetLocalEmulators: Boolean

        init {
            val hasLocal = targets.any { it.reservation.device is Device.LocalEmulator }
            val hasKubernetes = targets.any { it.reservation.device is Device.CloudEmulator }
            if (hasLocal && hasKubernetes) {
                throw IllegalStateException("Targeting to local and kubernetes emulators at the same configuration $name is not supported yet")
            } else {
                isTargetLocalEmulators = hasLocal
            }
        }

        override fun toString(): String = "$name, targets: $targets, filter: $filter "

        companion object
    }
}
