package com.avito.instrumentation.configuration

import com.avito.instrumentation.configuration.report.ReportConfig
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import javax.inject.Inject

public abstract class InstrumentationTestsPluginExtension @Inject constructor(
    layout: ProjectLayout,
    objects: ObjectFactory,
) {

    private val defaultOutput = layout.buildDirectory.map { it.dir("test-runner") }

    internal abstract val configurationsContainer: NamedDomainObjectContainer<InstrumentationConfiguration>

    internal val environmentsContainer: ExtensiblePolymorphicDomainObjectContainer<ExecutionEnvironment> =
        objects.polymorphicDomainObjectContainer(ExecutionEnvironment::class.java)

    public abstract val kubernetesHttpTries: Property<Int>

    public abstract val adbPullTimeoutSeconds: Property<Long>

    @get:Nested
    public abstract val experimental: ExperimentalExtension

    @get:Nested
    public abstract val macrobenchmark: MacrobenchmarkInstrumentationExtension

    // todo internal
    public abstract val filters: NamedDomainObjectContainer<InstrumentationFilter>

    public val report: Property<ReportConfig> = objects.property(ReportConfig::class.java)

    // todo MapProperty
    public var instrumentationParams: Map<String, String> = emptyMap()

    // https://developer.android.com/studio/command-line/logcat#filteringOutput
    public var logcatTags: Collection<String> = emptyList()

    public val outputDir: DirectoryProperty = objects.directoryProperty().convention(defaultOutput)

    init {
        report.finalizeValueOnRead()
    }

    public fun experimental(action: Action<ExperimentalExtension>) {
        action.execute(experimental)
    }

    public fun configurations(action: Action<NamedDomainObjectContainer<InstrumentationConfiguration>>) {
        action.execute(configurationsContainer)
    }

    public fun filters(action: Action<NamedDomainObjectContainer<InstrumentationFilter>>) {
        action.execute(filters)
    }

    public fun environments(action: Action<PolymorphicDomainObjectContainer<ExecutionEnvironment>>) {
        action.execute(environmentsContainer)
    }

    public fun macrobenchmark(action: Action<MacrobenchmarkInstrumentationExtension>) {
        action.execute(macrobenchmark)
        macrobenchmark.finalizeValues()
    }
}
