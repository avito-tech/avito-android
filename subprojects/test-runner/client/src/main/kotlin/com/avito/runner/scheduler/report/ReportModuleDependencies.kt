package com.avito.runner.scheduler.report

import com.avito.logger.LoggerFactory
import com.avito.runner.config.RunnerInputParams
import com.avito.time.TimeProvider
import okhttp3.OkHttpClient
import java.io.File

internal class ReportModuleDependencies(
    val timeProvider: TimeProvider,
    val loggerFactory: LoggerFactory,
    val httpClientBuilder: OkHttpClient.Builder,
    val testRunnerOutputDir: File,
    val tempLogcatDir: File,
    val params: RunnerInputParams,
)
