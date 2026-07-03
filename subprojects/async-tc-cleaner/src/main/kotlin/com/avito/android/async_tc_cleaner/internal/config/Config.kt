package com.avito.android.async_tc_cleaner.internal.config

import java.net.URL
import java.nio.file.Path
import kotlin.time.Duration

internal data class Config(
    val reaperEnabledToggleFile: Path?,
    val workDir: Path,
    val oldDirName: String,
    val stagingDirName: String,
    val pollInterval: Duration,
    val shutdownTimeout: Duration,
    val rmzPath: String,
    val node: String,
    val pod: String,
    val statsd: StatsdSettings?,
    val elastic: ElasticSettings?,
)

internal data class StatsdSettings(
    val host: String,
    val fallbackHost: String,
    val port: Int,
    val namespace: String,
)

internal data class ElasticSettings(
    val endpoints: List<URL>,
    val index: String,
    val apiKey: String?,
)
