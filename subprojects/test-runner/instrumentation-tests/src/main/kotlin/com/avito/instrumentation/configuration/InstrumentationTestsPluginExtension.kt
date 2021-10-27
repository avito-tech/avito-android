package com.avito.instrumentation.configuration

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
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

    internal val defaultOutput = layout.buildDirectory.map { it.dir("test-runner") }

    internal abstract val configurationsContainer: NamedDomainObjectContainer<InstrumentationConfiguration>

    @Deprecated("use sentryDsnUrl", replaceWith = ReplaceWith("sentryDsnUrl"))
    public var sentryDsn: String = ""

    public abstract val sentryDsnUrl: Property<String>

    @get:Nested
    public abstract val experimental: ExperimentalExtension

    public abstract val filters: NamedDomainObjectContainer<InstrumentationFilter>

    public val testReport: InstrumentationTestsReportExtension = InstrumentationTestsReportExtension()

    public val configurations: List<InstrumentationConfiguration>
        get() = configurationsContainer.toList()

    public var instrumentationParams: Map<String, String> = emptyMap()

    // https://developer.android.com/studio/command-line/logcat#filteringOutput
    public var logcatTags: Collection<String> = emptyList()

    @Deprecated("use outputDir property", replaceWith = ReplaceWith("outputDir"))
    public var output: String = defaultOutput.get().asFile.path

    public val outputDir: DirectoryProperty = objects.directoryProperty().convention(defaultOutput)

    public fun experimental(action: Action<ExperimentalExtension>) {
        action.execute(experimental)
    }

    public fun configurations(action: Action<NamedDomainObjectContainer<InstrumentationConfiguration>>) {
        action.execute(configurationsContainer)
    }

    public fun filters(action: Action<NamedDomainObjectContainer<InstrumentationFilter>>) {
        action.execute(filters)
    }

    public fun testReport(action: Action<InstrumentationTestsReportExtension>) {
        action.execute(testReport)
    }
}
