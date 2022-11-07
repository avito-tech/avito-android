package com.avito.test.summary

import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.http.StatsHttpEventListener
import com.avito.logger.LoggerFactory
import com.avito.reportviewer.ReportsApi
import com.avito.reportviewer.ReportsApiFactory
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import org.gradle.api.provider.Provider

internal class TestSummaryDI(
    private val statsDConfig: Provider<StatsDConfig>,
    private val loggerFactory: LoggerFactory,
    val timeProvider: TimeProvider = DefaultTimeProvider()
) {

    fun provideReportsApi(reportsHost: String): ReportsApi {
        val statsdSender = StatsDSender.create(
            statsDConfig.get(),
            loggerFactory,
        )
        return ReportsApiFactory.create(
            host = reportsHost,
            builder = OkHttpClient.Builder().eventListenerFactory {
                StatsHttpEventListener(
                    statsDSender = statsdSender,
                    timeProvider = timeProvider,
                    loggerFactory = loggerFactory,
                )
            },
        )
    }
}
