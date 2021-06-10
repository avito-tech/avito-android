package com.avito.instrumentation.configuration

import com.avito.report.model.Status
import com.avito.runner.config.InstrumentationFilterData
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

        private val annotations = Filter<String>()
        private val prefixes = Filter<String>()

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

        internal fun toData(): InstrumentationFilterData.FromSource {
            return InstrumentationFilterData.FromSource(
                prefixes = prefixes.value,
                annotations = annotations.value,
                excludeFlaky = excludeFlaky
            )
        }
    }

    public class FromRunHistory {

        private class ReportFilter(
            val id: String,
            val statuses: Filter<RunStatus> = Filter()
        )

        private val previous = Filter<RunStatus>()

        private var reportFilter: ReportFilter? = null

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

        internal fun toData(): InstrumentationFilterData.FromRunHistory {
            return InstrumentationFilterData.FromRunHistory(
                previousStatuses = Filter.Value(
                    included = previous.value.included.map { it.map() }.toSet(),
                    excluded = previous.value.excluded.map { it.map() }.toSet()
                ),
                reportFilter = reportFilter?.let { filter ->
                    InstrumentationFilterData.FromRunHistory.ReportFilter(
                        statuses = Filter.Value(
                            included = filter.statuses.value.included.map { it.map() }.toSet(),
                            excluded = filter.statuses.value.excluded.map { it.map() }.toSet()
                        )
                    )
                }
            )
        }

        private fun RunStatus.map(): com.avito.runner.config.RunStatus {
            return when (this) {
                RunStatus.Failed -> com.avito.runner.config.RunStatus.Failed
                RunStatus.Success -> com.avito.runner.config.RunStatus.Success
                RunStatus.Lost -> com.avito.runner.config.RunStatus.Lost
                RunStatus.Skipped -> com.avito.runner.config.RunStatus.Skipped
                RunStatus.Manual -> com.avito.runner.config.RunStatus.Manual
            }
        }

        public enum class RunStatus(public val statusClass: Class<out Status>) {
            Failed(Status.Failure::class.java),
            Success(Status.Success::class.java),
            Lost(Status.Lost::class.java),
            Skipped(Status.Skipped::class.java),
            Manual(Status.Manual::class.java)
        }
    }

    internal fun toData(): InstrumentationFilterData {
        return InstrumentationFilterData(
            name = name,
            fromSource = fromSource.toData(),
            fromRunHistory = fromRunHistory.toData()
        )
    }
}
