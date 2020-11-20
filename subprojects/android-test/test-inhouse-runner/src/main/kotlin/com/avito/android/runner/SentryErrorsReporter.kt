package com.avito.android.runner

import io.sentry.SentryClient

class SentryErrorsReporter(
    private val sentry: SentryClient
) : ErrorsReporter {

    override fun reportError(error: Throwable) {
        sentry.sendException(error)
    }
}
