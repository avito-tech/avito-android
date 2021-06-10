package com.avito.instrumentation.configuration

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.runner.config.InstrumentationConfigurationData
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.config.InstrumentationParameters
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

public abstract class InstrumentationConfiguration(public val name: String) {

    public var instrumentationParams: Map<String, String> = emptyMap()

    public var reportSkippedTests: Boolean = false

    public var runOnlyChangedTests: Boolean = false

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
        filters: List<InstrumentationFilterData>
    ): InstrumentationConfigurationData {

        val mergedInstrumentationParameters: InstrumentationParameters =
            parentInstrumentationParameters
                .applyParameters(instrumentationParams)

        return InstrumentationConfigurationData(
            name = name,
            instrumentationParams = mergedInstrumentationParameters,
            reportSkippedTests = reportSkippedTests,
            runOnlyChangedTests = runOnlyChangedTests,
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
}
