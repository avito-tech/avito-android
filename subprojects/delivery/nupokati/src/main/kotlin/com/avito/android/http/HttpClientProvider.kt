package com.avito.android.http

import Slf4jGradleLoggerFactory
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.http.BasicAuthenticator
import com.avito.http.HttpClientProvider
import com.avito.logger.LoggerFactory
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient

internal fun createArtifactoryHttpClient(
    user: String,
    password: String,
    statsDConfig: StatsDConfig,
    loggerFactory: LoggerFactory = Slf4jGradleLoggerFactory,
    timeProvider: TimeProvider = DefaultTimeProvider()
): OkHttpClient {
    return HttpClientProvider(
        statsDSender = StatsDSender.create(
            config = statsDConfig,
            loggerFactory = loggerFactory
        ),
        timeProvider = timeProvider,
        loggerFactory = loggerFactory
    ).provide()
        .authenticator(BasicAuthenticator(user, password))
        .build()
}
