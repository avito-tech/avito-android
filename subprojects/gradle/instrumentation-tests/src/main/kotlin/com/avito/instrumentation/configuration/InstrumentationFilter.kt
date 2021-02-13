package com.avito.instrumentation.configuration

import com.avito.android.runner.report.factory.ReportFactory
import com.avito.instrumentation.suite.filter.Filter
import com.avito.report.model.Status
import org.gradle.api.Action
import java.io.Serializable

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

        internal fun toData(): Data.FromSource {
            return Data.FromSource(
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

        internal fun toData(): Data.FromRunHistory {
            return Data.FromRunHistory(
                previousStatuses = previous.value,
                reportFilter = reportFilter?.let { filter ->
                    Data.FromRunHistory.ReportFilter(
                        reportConfig = ReportFactory.Config.ReportViewerId(filter.id),
                        statuses = filter.statuses.value
                    )
                }
            )
        }

        public enum class RunStatus(public val statusClass: Class<out Status>) {
            Failed(Status.Failure::class.java),
            Success(Status.Success::class.java),
            Lost(Status.Lost::class.java),
            Skipped(Status.Skipped::class.java),
            Manual(Status.Manual::class.java)
        }
    }

    internal fun toData(): Data {
        return Data(
            name = name,
            fromSource = fromSource.toData(),
            fromRunHistory = fromRunHistory.toData()
        )
    }

    public data class Data(
        val name: String,
        val fromSource: FromSource,
        val fromRunHistory: FromRunHistory
    ) : Serializable {

        public data class FromSource(
            val prefixes: Filter.Value<String>,
            val annotations: Filter.Value<String>,
            val excludeFlaky: Boolean
        ) : Serializable

        public data class FromRunHistory(
            val previousStatuses: Filter.Value<InstrumentationFilter.FromRunHistory.RunStatus>,
            val reportFilter: ReportFilter?
        ) : Serializable {

            public data class ReportFilter(
                val reportConfig: ReportFactory.Config.ReportViewerId,
                val statuses: Filter.Value<InstrumentationFilter.FromRunHistory.RunStatus>
            ) : Serializable
        }

        public companion object
    }
}
