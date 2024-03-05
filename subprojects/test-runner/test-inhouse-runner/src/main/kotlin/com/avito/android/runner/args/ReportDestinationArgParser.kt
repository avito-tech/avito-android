package com.avito.android.runner.args

import com.avito.android.test.report.ArgsProvider
import com.avito.android.transport.ReportDestination
import com.avito.reportviewer.model.ReportCoordinates
import okhttp3.HttpUrl.Companion.toHttpUrl

public class ReportDestinationArgParser : ArgProvider<ReportDestination> {
    override fun parse(args: ArgsProvider): ReportDestination {
        val parsedDestination = args.getArgument(reportDestinationKey) ?: reportDestinationNoop
        return when (parsedDestination) {
            reportDestinationBackend -> ReportDestination.Backend(
                reportApiUrl = args.getArgumentOrThrow("reportApiUrl"),
                reportViewerUrl = args.getArgumentOrThrow("reportViewerUrl"),
                deviceName = args.getArgumentOrThrow("deviceName"),
                coordinates = ReportCoordinates(
                    planSlug = args.getArgumentOrThrow("planSlug"),
                    jobSlug = args.getArgumentOrThrow("jobSlug"),
                    runId = args.getArgumentOrThrow("runId")
                ),
                fileStorageUrl = args.getArgumentOrThrow("fileStorageUrl").toHttpUrl()
            )
            reportDestinationFile -> ReportDestination.File
            reportDestinationLegacy -> ReportDestination.Legacy(
                fileStorageUrl = args.getArgumentOrThrow("fileStorageUrl").toHttpUrl()
            )
            reportDestinationNoop -> ReportDestination.NoOp
            else -> throw IllegalStateException("Incorrect $reportDestinationKey value: $parsedDestination")
        }
    }

    private companion object {
        private const val reportDestinationKey = "avito.report.transport"
        private const val reportDestinationFile = "file"
        private const val reportDestinationBackend = "backend"
        private const val reportDestinationLegacy = "legacy"
        private const val reportDestinationNoop = "noop"
    }
}
