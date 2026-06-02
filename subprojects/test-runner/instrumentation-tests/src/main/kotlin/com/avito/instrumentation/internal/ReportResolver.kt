package com.avito.instrumentation.internal

import com.avito.android.plugins.configuration.RunIdResolver
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation.configuration.report.ReportConfig
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class ReportResolver(
    private val extension: InstrumentationTestsPluginExtension,
    private val runIdResolver: RunIdResolver,
    private val providerFactory: ProviderFactory,
) {

    fun getRunId(): Provider<String> {
        return when (extension.report.get()) {
            ReportConfig.NoOp -> providerFactory.provider { "" }
            is ReportConfig.ReportViewer -> runIdResolver.getRunIdProvider().map { it.toReportViewerFormat() }
        }
    }

    fun getReport(): ReportConfig? {
        return extension.report.orNull
    }
}
