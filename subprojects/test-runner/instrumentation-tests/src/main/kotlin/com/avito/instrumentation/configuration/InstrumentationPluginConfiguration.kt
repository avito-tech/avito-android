package com.avito.instrumentation.configuration

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.Nested

public object InstrumentationPluginConfiguration {
    public abstract class GradleInstrumentationPluginConfiguration(
        project: Project
    ) {

        // todo make optional
        public var sentryDsn: String = ""

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

        public var output: String = project.file("build/test-runner/").path

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
