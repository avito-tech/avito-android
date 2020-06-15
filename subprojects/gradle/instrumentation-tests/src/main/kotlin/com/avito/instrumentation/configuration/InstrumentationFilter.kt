package com.avito.instrumentation.configuration

import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.filter.Filter
import com.avito.report.model.Status
import org.gradle.api.Action
import java.io.Serializable

abstract class InstrumentationFilter(val name: String) {

    val fromSource = FromSource()
    val fromRunHistory = FromRunHistory()

    fun fromSource(action: Action<FromSource>) {
        action.execute(fromSource)
    }

    fun fromRunHistory(action: Action<FromRunHistory>) {
        action.execute(fromRunHistory)
    }

    class FromSource {

        private val annotations = Filter<String>()
        private val prefixes = Filter<String>()

        fun includeByAnnotations(annotations: Set<String>) {
            this.annotations.include(annotations)
        }

        fun excludeByAnnotations(annotations: Set<String>) {
            this.annotations.exclude(annotations)
        }

        fun includeByPrefixes(prefixes: Set<String>) {
            this.prefixes.include(prefixes)
        }

        fun excludeByPrefixes(prefixes: Set<String>) {
            this.prefixes.exclude(prefixes)
        }

        internal fun toData(): Data.FromSource {
            return Data.FromSource(
                prefixes = prefixes.value,
                annotations = annotations.value
            )
        }
    }

    class FromRunHistory {

        private class ReportFilter(
            val id: String,
            val statuses: Filter<RunStatus> = Filter()
        )

        private val previous = Filter<RunStatus>()

        private var reportFilter: ReportFilter? = null

        fun includePreviousStatuses(statuses: Set<RunStatus>) {
            previous.include(statuses)
        }

        fun excludePreviousStatuses(statuses: Set<RunStatus>) {
            previous.exclude(statuses)
        }

        fun report(id: String, filter: Action<Filter<RunStatus>>) {
            reportFilter = ReportFilter(
                id = id
            ).also { reportFilter -> filter.execute(reportFilter.statuses) }
        }

        internal fun toData(): Data.FromRunHistory {
            return Data.FromRunHistory(
                previousStatuses = previous.value,
                reportFilter = reportFilter?.let { filter ->
                    Data.FromRunHistory.ReportFilter(
                        reportConfig = Report.Factory.Config.ReportViewerId(filter.id),
                        statuses = filter.statuses.value
                    )
                }
            )
        }

        enum class RunStatus(val statusClass: Class<out Status>) {
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

    data class Data(
        val name: String,
        val fromSource: FromSource,
        val fromRunHistory: FromRunHistory
    ) : Serializable {

        data class FromSource(
            val prefixes: Filter.Value<String>,
            val annotations: Filter.Value<String>
        ) : Serializable

        data class FromRunHistory(
            val previousStatuses: Filter.Value<InstrumentationFilter.FromRunHistory.RunStatus>,
            val reportFilter: ReportFilter?
        ) : Serializable {

            data class ReportFilter(
                val reportConfig: Report.Factory.Config.ReportViewerId,
                val statuses: Filter.Value<InstrumentationFilter.FromRunHistory.RunStatus>
            ) : Serializable

        }

        companion object
    }

}