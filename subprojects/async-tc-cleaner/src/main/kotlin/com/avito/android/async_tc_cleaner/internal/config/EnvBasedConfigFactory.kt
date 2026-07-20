package com.avito.android.async_tc_cleaner.internal.config

import com.avito.android.Result
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class EnvBasedConfigFactory(
    private val envProvider: (String) -> String? = System::getenv,
) {

    fun create(): Result<Config> {
        return try {
            val workDir = workDir()
            val oldDirName = env("OLD_DIR_NAME") ?: ".old"
            val stagingDirName = env("STAGING_DIR_NAME") ?: ".reaper-staging"
            validateSimpleName("OLD_DIR_NAME", oldDirName)
            validateSimpleName("STAGING_DIR_NAME", stagingDirName)
            require(oldDirName != stagingDirName) {
                "OLD_DIR_NAME and STAGING_DIR_NAME must differ; both are '$oldDirName'."
            }

            Result.Success(
                Config(
                    reaperEnabledToggleFile = enabledToggleFile(),
                    workDir = workDir,
                    oldDirName = oldDirName,
                    stagingDirName = stagingDirName,
                    pollInterval = seconds("POLL_INTERVAL_SECONDS", default = 45, min = 1),
                    shutdownTimeout = seconds("SHUTDOWN_TIMEOUT_SECONDS", default = 25, min = 1),
                    node = env("NODE_NAME") ?: "unknown",
                    pod = env("POD_NAME") ?: "unknown",
                    statsd = statsd(),
                    elastic = elastic(),
                )
            )
        } catch (t: Throwable) {
            Result.Failure(RuntimeException("Failed to parse cleaner configuration from environment: ${t.message}", t))
        }
    }

    private fun workDir(): Path {
        val raw = env("WORK_DIR") ?: error("Missing WORK_DIR environment variable.")
        val path = Paths.get(raw).normalize()
        require(path.isAbsolute) { "WORK_DIR must be absolute, got '$raw'." }
        require(!path.isFileSystemRoot()) { "WORK_DIR must not be the filesystem root, got '$raw'." }
        return path
    }

    private fun enabledToggleFile(): Path? {
        val raw = env("REAPER_ENABLED_FILE") ?: return null
        val path = Paths.get(raw)
        require(path.isAbsolute) { "REAPER_ENABLED_FILE must be absolute, got '$raw'." }
        return path
    }

    private fun statsd(): StatsdSettings? {
        val host = env("STATSD_HOST") ?: return null
        return StatsdSettings(
            host = host,
            fallbackHost = env("STATSD_FALLBACK_HOST") ?: host,
            port = positiveInt("STATSD_PORT", default = 8126),
            namespace = env("STATSD_NAMESPACE") ?: "android.async_tc_cleaner",
        )
    }

    private fun elastic(): ElasticSettings? {
        val raw = env("ELASTIC_ENDPOINTS") ?: return null
        val endpoints = raw.split(',', ' ', '\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { URI(it).toURL() }
        if (endpoints.isEmpty()) return null
        return ElasticSettings(
            endpoints = endpoints,
            index = env("ELASTIC_INDEX") ?: "avito-android-async-tc-cleaner",
            apiKey = env("ELASTIC_API_KEY"),
        )
    }

    private fun validateSimpleName(envName: String, value: String) {
        val path = Paths.get(value)
        val isSimple = !path.isAbsolute &&
            path.nameCount == 1 &&
            path.fileName.toString() == value &&
            value != "." &&
            value != ".."
        require(isSimple) {
            "$envName must be a simple relative directory name (no '/', no '..', not absolute), got '$value'."
        }
    }

    private fun env(name: String): String? = envProvider(name)?.takeIf { it.isNotBlank() }

    private fun positiveInt(name: String, default: Int): Int {
        return parseLong(name)?.also {
            require(it in 1..Int.MAX_VALUE.toLong()) { "$name must be a positive Int, got '$it'." }
        }?.toInt() ?: default
    }

    private fun seconds(name: String, default: Long, min: Long): Duration {
        val parsed = parseLong(name) ?: default
        require(parsed >= min) { "$name must be >= $min, got '$parsed'." }
        return parsed.seconds
    }

    private fun parseLong(name: String): Long? {
        val value = env(name) ?: return null
        return value.toLongOrNull() ?: error("$name must be a number, got '$value'.")
    }

    private fun Path.isFileSystemRoot(): Boolean = isAbsolute && parent == null
}
