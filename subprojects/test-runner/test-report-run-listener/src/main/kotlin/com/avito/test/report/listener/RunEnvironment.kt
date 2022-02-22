package com.avito.test.report.listener

import com.avito.android.elastic.ElasticConfig
import com.avito.android.sentry.SentryConfig
import com.avito.android.stats.StatsDConfig
import com.avito.android.test.report.arguments.ArgsProvider
import com.avito.android.transport.ReportDestination
import com.avito.reportviewer.model.ReportCoordinates
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File

// TODO: Reuse implementation from InHouseInstrumentationTestRunner
// TODO: Add test metadata
internal data class RunEnvironment(
    val testRunCoordinates: ReportCoordinates,
    internal val testResultsDirectory: File,
    internal val elasticConfig: ElasticConfig,
    internal val sentryConfig: SentryConfig,
    internal val statsDConfig: StatsDConfig,
    internal val fileStorageUrl: HttpUrl
)

internal fun parseEnvironment(
    argumentsProvider: ArgsProvider,
): RunEnvironment {
    val coordinates = ReportCoordinates(
        planSlug = argumentsProvider.getMandatoryArgument("planSlug"),
        jobSlug = argumentsProvider.getMandatoryArgument("jobSlug"),
        runId = argumentsProvider.getMandatoryArgument("runId")
    )

    return RunEnvironment(
        elasticConfig = ElasticConfig.Disabled,
        sentryConfig = SentryConfig.Disabled,
        statsDConfig = StatsDConfig.Disabled,
        fileStorageUrl = argumentsProvider.getMandatoryArgument("fileStorageUrl").toHttpUrl(),
        testRunCoordinates = coordinates,
        testResultsDirectory = parseTestResultsDirectory(argumentsProvider)
    )
}

internal fun parseReportDestination(argumentsProvider: ArgsProvider, environment: String): ReportDestination {
    val isReportEnabled = argumentsProvider.getOptionalArgument("avito.report.enabled")?.toBoolean() ?: false
    return if (isReportEnabled) {
        ReportDestination.Backend(
            reportApiUrl = argumentsProvider.getMandatoryArgument("reportApiUrl"),
            reportViewerUrl = argumentsProvider.getMandatoryArgument("reportViewerUrl"),
            deviceName = environment
        )
    } else {
        return ReportDestination.NoOp
    }
}

private fun parseTestResultsDirectory(argumentsProvider: ArgsProvider): File {
    return File(argumentsProvider.getMandatoryArgument("testResultsDir")).apply {
        if (!exists()) mkdirs()
    }
}
