package com.avito.instrumentation.configuration

import com.avito.instrumentation.configuration.target.TargetConfiguration
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import java.time.Duration

public abstract class InstrumentationConfiguration(public val name: String) {

    internal abstract val targetsContainer: NamedDomainObjectContainer<TargetConfiguration>

    public var instrumentationParams: Map<String, String> = emptyMap()

    public var reportSkippedTests: Boolean = false

    public var runOnlyChangedTests: Boolean = false

    public var kubernetesNamespace: String = "default"

    @Deprecated("Use testRunnerExecutionTimeout and instrumentationTaskTimeout properties instead")
    public var timeoutInSeconds: Long = 120L // TODO: remove after MBS-11465

    public var testRunnerExecutionTimeout: Duration = Duration.ofMinutes(100)

    public var instrumentationTaskTimeout: Duration = Duration.ofMinutes(120)

    public var enableDeviceDebug: Boolean = false

    public var filter: String = "default"

    public fun targets(action: Action<NamedDomainObjectContainer<TargetConfiguration>>) {
        action.execute(targetsContainer)
    }
}
