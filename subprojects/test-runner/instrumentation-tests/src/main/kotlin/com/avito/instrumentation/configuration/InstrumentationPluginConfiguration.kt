package com.avito.instrumentation.configuration

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public object InstrumentationPluginConfiguration {
    public abstract class GradleInstrumentationPluginConfiguration(
        project: Project
    ) {

        // todo make optional
        public var sentryDsn: String = ""

        /**
         * For testing gradle plugin itself only!
         *
         * Will dump runner input parameters, instead of running tests
         */
        public abstract val testDumpParams: Property<Boolean>

        @get:Nested
        public abstract val experimental: ExperimentalExtension

        /**
         * todo make internal
         *  change "configurationsContainer.register" to "configurations { register ... }"
         */
        public abstract val configurationsContainer: NamedDomainObjectContainer<InstrumentationConfiguration>

        public abstract val filters: NamedDomainObjectContainer<InstrumentationFilter>

        public val testReport: InstrumentationTestReportExtension = InstrumentationTestReportExtension()

        public val configurations: List<InstrumentationConfiguration>
            get() = configurationsContainer.toList()

        public var instrumentationParams: Map<String, String> = emptyMap()

        // https://developer.android.com/studio/command-line/logcat#filteringOutput
        public var logcatTags: Collection<String> = emptyList()

        public var output: String =
            project.rootProject.file("outputs/${project.name}/instrumentation").path

        public fun experimental(action: Action<ExperimentalExtension>) {
            action.execute(experimental)
        }

        public fun configurations(action: Action<NamedDomainObjectContainer<InstrumentationConfiguration>>) {
            action.execute(configurationsContainer)
        }

        public fun filters(action: Action<NamedDomainObjectContainer<InstrumentationFilter>>) {
            action.execute(filters)
        }

        public fun testReport(action: Action<InstrumentationTestReportExtension>) {
            action.execute(testReport)
        }
    }
}
