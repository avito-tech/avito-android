package com.avito.android.plugin.build_metrics

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import java.time.Duration
import javax.inject.Inject

public abstract class BuildMetricsExtension @Inject constructor(
    objectFactory: ObjectFactory
) {
    // TODO validate length
    public val buildType: Property<String> = objectFactory.property()

    public val environment: Property<BuildEnvironment> =
        objectFactory.property<BuildEnvironment>().convention(BuildEnvironment.CI)

    public val userName: Property<String> = objectFactory.property<String>().convention(System.getProperty("user.name"))

    public val sendJvmMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(false)

    public val sendOsMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(false)

    /**
     * Will work only in builds without configuration cache
     */
    public val sendCriticalPathMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendSlowTaskMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendCompileMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendRequestedTasksMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    /**
     * Tasks that are injected by our Gradle build logic (bootstrap / verification checks)
     * and should be excluded from `gradle.requested_task.duration` metrics.
     *
     * If all requested tasks are bootstrap tasks, we treat it as a Gradle sync event and report `gradleSync`.
     */
    public val bootstrapRequestedTaskNames: SetProperty<String> =
        objectFactory.setProperty(String::class.java).convention(
            setOf(
                // Root tasks added by this repo
                "installGitHooks",
                "checkBuildEnvironment",
                // Common verification tasks injected by build logic
                "checkModulesOwners",
                "checkAnvilConfiguration",
                "checkModuleTypeNotDeprecated",
            )
        )

    public val slowTaskMinimumDuration: Property<Duration> =
        objectFactory.property<Duration>().convention(Duration.ofSeconds(10))

    public val criticalTaskMinimumDuration: Property<Duration> =
        objectFactory.property<Duration>().convention(Duration.ofSeconds(10))

    public val compileMetricsMinimumDuration: Property<Duration> =
        objectFactory.property<Duration>().convention(Duration.ofSeconds(10))

    public val sendBuildCacheMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    internal val buildCacheObservableTasks: SetProperty<String> =
        objectFactory.setProperty(String::class.java).convention(emptySet())

    public val sendBuildInitConfiguration: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendBuildTotal: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendAppBuildTime: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendTestRunnerMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val writeModulesBuildTime: Property<Boolean> = objectFactory.property<Boolean>().convention(false)

    public val modulesBuildTimeFile: RegularFileProperty = objectFactory.fileProperty()

    public val branchName: Property<String> = objectFactory.property()

    public val repoName: Property<String> = objectFactory.property()

    public fun buildCacheObservableTasks(tasks: Set<String>) {
        buildCacheObservableTasks.set(tasks)
        buildCacheObservableTasks.disallowChanges()
    }
}
