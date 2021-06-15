package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration

internal object AnalyticsResolver {

    fun getSentryDsn(extension: InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration): String {
        val value = extension.sentryDsn
        return if (value.isBlank()) {
            "http://stub-project@stub-host/0"
        } else {
            value
        }
    }
}
