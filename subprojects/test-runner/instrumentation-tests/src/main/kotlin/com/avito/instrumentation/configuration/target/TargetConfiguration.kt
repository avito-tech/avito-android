package com.avito.instrumentation.configuration.target

import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import org.gradle.api.Action
import org.gradle.api.tasks.Nested
import java.io.Serializable

public abstract class TargetConfiguration(public val name: String) : Serializable {

    @get:Nested
    internal abstract val scheduling: SchedulingConfiguration

    public lateinit var deviceName: String

    /**
     * For more precise one time runs, like dynamic configuration.
     * For example: configuration with 4 targets runs on ci, but you need to check specific one.
     * Which one could be controlled with external gradle params
     */
    public var enabled: Boolean = true

    public var instrumentationParams: Map<String, String> = emptyMap()

    public fun scheduling(action: Action<SchedulingConfiguration>) {
        action.execute(scheduling)
    }
}
