package com.avito.instrumentation.configuration

import com.avito.android.runner.devices.model.DeviceType
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.reservation.request.Device
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import java.io.Serializable

abstract class InstrumentationConfiguration(val name: String) {

    var instrumentationParams: Map<String, String> = emptyMap()

    var reportSkippedTests = false

    var impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off

    var kubernetesNamespace = "android-emulator"

    var timeoutInSeconds: Long = 6000L // 100min

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
            reportSkippedTests = reportSkippedTests,
            impactAnalysisPolicy = impactAnalysisPolicy,
            kubernetesNamespace = kubernetesNamespace,
            targets = targets.map {
                it.data(parentInstrumentationParameters = mergedInstrumentationParameters)
            },
            enableDeviceDebug = enableDeviceDebug,
            timeoutInSeconds = timeoutInSeconds,
            filter = filters.singleOrNull { it.name == filter }
                ?: throw IllegalStateException("Can't find filter=$filter")
        )
    }

    data class Data(
        val name: String,
        val instrumentationParams: InstrumentationParameters,
        val reportSkippedTests: Boolean,
        val impactAnalysisPolicy: ImpactAnalysisPolicy,
        val kubernetesNamespace: String,
        val targets: List<TargetConfiguration.Data>,
        val enableDeviceDebug: Boolean,
        val timeoutInSeconds: Long,
        val filter: InstrumentationFilter.Data
    ) : Serializable {

        val requestedDeviceType: DeviceType = determineRequestedDeviceType(targets.map { it.reservation.device })

        override fun toString(): String = "$name, targets: $targets, filter: $filter "

        private fun determineRequestedDeviceType(requestedDevices: List<Device>): DeviceType {
            return when {
                requestedDevices.all { it is Device.LocalEmulator } -> DeviceType.LOCAL
                requestedDevices.all { it is Device.CloudEmulator } -> DeviceType.CLOUD
                requestedDevices.all { it is Device.MockEmulator } -> DeviceType.MOCK
                else -> {
                    val deviceTypesNames = DeviceType.values().map { it.name }
                    throw IllegalStateException(
                        "Targeting different type of emulators($deviceTypesNames) " +
                            "in the same configuration is not supported; " +
                            "Affected configuration: $name"
                    )
                }
            }
        }

        companion object
    }
}
