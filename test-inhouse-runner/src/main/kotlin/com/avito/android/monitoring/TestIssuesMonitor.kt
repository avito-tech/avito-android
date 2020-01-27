package com.avito.android.monitoring

import io.sentry.SentryClient
import io.sentry.SentryClientFactory
import okhttp3.Response

interface TestIssuesMonitor {

    fun onFailure(throwable: Throwable)

    /**
     * Ошибки не приводящие к падению теста, но вызывающие retry, которые увеливиают время прохождения
     */
    fun onWarning(throwable: Throwable)

    fun onWarning(response: Response)
}

/**
 * Т.к. отправляем request/response, которые могут быть довольно большими,
 * но это необходимо чтобы разобраться в причинах
 * default=400
 * https://github.com/getsentry/sentry-java/issues/543
 */
private const val sentryExtraValueLimit: Int = 50000

/**
 * http://links.k.avito.ru/kV
 */
fun createSentry(sentryDsn: String): SentryClient {
    return SentryClientFactory.sentryClient(
        sentryDsn,
        CustomizableSentryClientFactory(sentryExtraValueLimit)
    )
}
