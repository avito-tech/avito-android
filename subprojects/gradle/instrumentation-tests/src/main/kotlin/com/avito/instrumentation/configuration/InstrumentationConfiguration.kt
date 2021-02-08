package com.avito.instrumentation.configuration

import com.avito.android.runner.devices.model.DeviceType
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.reservation.request.Device
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import java.io.Serializable

public abstract class InstrumentationConfiguration(public val name: String) {

    public var instrumentationParams: Map<String, String> = emptyMap()

    public var reportSkippedTests: Boolean = false

    public var impactAnalysisPolicy: ImpactAnalysisPolicy = ImpactAnalysisPolicy.Off

    public var kubernetesNamespace: String = "android-emulator"

    public var timeoutInSeconds: Long = 6000L // 100min

    public var enableDeviceDebug: Boolean = false

    public var filter: String = "default"

    public abstract val targetsContainer: NamedDomainObjectContainer<TargetConfiguration>

    public val targets: List<TargetConfiguration>
        get() = targetsContainer.toList()
            .filter { it.enabled }

    public fun targets(action: Action<NamedDomainObjectContainer<TargetConfiguration>>) {
        action.execute(targetsContainer)
    }

    public fun validate() {
        require(kubernetesNamespace.isNotBlank()) { "kubernetesNamespace must be set" }
        require(targets.isNotEmpty()) { "configuration $name must have at least one target" }
        targets.forEach { it.validate() }
    }

    public fun data(
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

    public data class Data(
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

        public companion object
    }
}
