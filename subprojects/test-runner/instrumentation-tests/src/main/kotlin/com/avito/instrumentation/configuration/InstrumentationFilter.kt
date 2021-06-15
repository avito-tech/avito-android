package com.avito.instrumentation.configuration

import com.avito.report.model.Status
import com.avito.runner.scheduler.suite.filter.Filter
import org.gradle.api.Action

public abstract class InstrumentationFilter(public val name: String) {

    public val fromSource: FromSource = FromSource()

    public val fromRunHistory: FromRunHistory = FromRunHistory()

    public fun fromSource(action: Action<FromSource>) {
        action.execute(fromSource)
    }

    public fun fromRunHistory(action: Action<FromRunHistory>) {
        action.execute(fromRunHistory)
    }

    public class FromSource {

        internal val annotations = Filter<String>()
        internal val prefixes = Filter<String>()

        public var excludeFlaky: Boolean = false

        public fun includeByAnnotations(annotations: Set<String>) {
            this.annotations.include(annotations)
        }

        public fun excludeByAnnotations(annotations: Set<String>) {
            this.annotations.exclude(annotations)
        }

        public fun includeByPrefixes(prefixes: Set<String>) {
            this.prefixes.include(prefixes)
        }

        public fun excludeByPrefixes(prefixes: Set<String>) {
            this.prefixes.exclude(prefixes)
        }
    }

    public class FromRunHistory {

        internal class ReportFilter(
            val id: String,
            val statuses: Filter<RunStatus> = Filter()
        )

        internal val previous = Filter<RunStatus>()

        internal var reportFilter: ReportFilter? = null

        public fun includePreviousStatuses(statuses: Set<RunStatus>) {
            previous.include(statuses)
        }

        public fun excludePreviousStatuses(statuses: Set<RunStatus>) {
            previous.exclude(statuses)
        }

        public fun report(id: String, filter: Action<Filter<RunStatus>>) {
            reportFilter = ReportFilter(
                id = id
            ).also { reportFilter -> filter.execute(reportFilter.statuses) }
        }

        public enum class RunStatus(public val statusClass: Class<out Status>) {
            Failed(Status.Failure::class.java),
            Success(Status.Success::class.java),
            Lost(Status.Lost::class.java),
            Skipped(Status.Skipped::class.java),
            Manual(Status.Manual::class.java)
        }
    }
}
