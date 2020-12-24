package com.avito.instrumentation.finalizer

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.report.HasFailedTestDeterminer
import com.avito.instrumentation.report.HasNotReportedTestsDeterminer
import com.avito.instrumentation.report.JUnitReportWriter
import com.avito.instrumentation.report.Report
import com.avito.logger.LoggerFactory
import com.avito.report.ReportViewer
import com.avito.utils.BuildFailer
import com.google.common.annotations.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.GsonBuilder

interface FinalizerFactory {

    fun create(): InstrumentationTestActionFinalizer

    class Impl : FinalizerFactory {
        private val params: InstrumentationTestsAction.Params
        private val sourceReport: Report
        private val gson: Gson
        private val buildFailer: BuildFailer

        // todo Make generic. Need two realization for InMemory and ReportViewer
        private val reportViewer: ReportViewer
        private val loggerFactory: LoggerFactory

        @VisibleForTesting
        internal constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson = GsonBuilder().setPrettyPrinting().create(),
            buildFailer: BuildFailer
        ) {
            this.params = params
            this.sourceReport = sourceReport
            this.gson = gson
            this.reportViewer = ReportViewer.Impl(params.reportViewerUrl)
            this.loggerFactory = params.loggerFactory
            this.buildFailer = buildFailer
        }

        constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson
        ) : this(
            params = params,
            sourceReport = sourceReport,
            gson = gson,
            buildFailer = BuildFailer.RealFailer()
        )

        override fun create(): InstrumentationTestActionFinalizer {

            val hasFailedTestDeterminer: HasFailedTestDeterminer = HasFailedTestDeterminer.Impl(
                suppressFailure = params.suppressFailure,
                suppressFlaky = params.suppressFlaky
            )

            return InstrumentationTestActionFinalizer.Impl(
                hasFailedTestDeterminer = hasFailedTestDeterminer,
                hasNotReportedTestsDeterminer = HasNotReportedTestsDeterminer.Impl(),
                sourceReport = sourceReport,
                params = params,
                reportViewer = reportViewer,
                gson = gson,
                jUnitReportWriter = JUnitReportWriter(reportViewer),
                buildFailer = buildFailer,
                loggerFactory = loggerFactory
            )
        }
    }
}
