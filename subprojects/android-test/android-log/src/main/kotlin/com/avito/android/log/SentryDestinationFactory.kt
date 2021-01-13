package com.avito.android.log

import com.avito.android.sentry.SentryConfig
import com.avito.logger.LoggingDestination
import com.avito.logger.destination.SentryDestination

internal object SentryDestinationFactory {

    fun create(config: SentryConfig, metadata: AndroidMetadata): LoggingDestination {
        return SentryDestination(config, metadata.toMap())
    }
}
