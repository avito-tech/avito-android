package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class SentryResolver(
    private val extension: InstrumentationTestsPluginExtension,
    private val providers: ProviderFactory,
) {

    fun getSentryDsn(): Provider<String> {
        return extension.sentryDsnUrl.convention(getDeprecatedSentryDsnValueIfSet())
    }

    private fun getDeprecatedSentryDsnValueIfSet(): Provider<String?> {
        return providers.provider {
            @Suppress("DEPRECATION")
            val value = extension.sentryDsn
            if (value.isBlank()) {
                null
            } else {
                value
            }
        }
    }
}
