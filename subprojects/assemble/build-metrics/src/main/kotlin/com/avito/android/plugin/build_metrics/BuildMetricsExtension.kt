package com.avito.android.plugin.build_metrics

import com.android.build.gradle.internal.utils.setDisallowChanges
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
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

    /**
     * Common prefix for statsd metrics
     */
    @Deprecated("Used for ci/local prefix. Will be deleted")
    public val metricsPrefix: ListProperty<String> = objectFactory.listProperty<String>().apply {
        setDisallowChanges(Providers.changing<Iterable<String>> {
            throw RuntimeException("Deprecated property. Don't use it")
        })
        disallowChanges()
    }

    public val sendJvmMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(false)

    public val sendOsMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(false)

    public val sendCriticalPathMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendSlowTaskMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val slowTaskMinimumDuration: Property<Duration> =
        objectFactory.property<Duration>().convention(Duration.ofSeconds(10))

    public val criticalTaskMinimumDuration: Property<Duration> =
        objectFactory.property<Duration>().convention(Duration.ofSeconds(10))

    public val sendBuildCacheMetrics: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendBuildInitConfiguration: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendBuildTotal: Property<Boolean> = objectFactory.property<Boolean>().convention(true)

    public val sendAppBuildTime: Property<Boolean> = objectFactory.property<Boolean>().convention(true)
}
