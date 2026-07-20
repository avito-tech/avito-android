package com.avito.android.async_tc_cleaner.internal.config

import com.avito.android.Result
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

class EnvBasedConfigFactoryTest {

    @Test
    fun `parses minimal config with safe defaults`() {
        val config = create(mapOf("WORK_DIR" to "/opt/buildAgent/work")).getOrThrow()

        assertEquals(Paths.get("/opt/buildAgent/work"), config.workDir)
        assertEquals(".old", config.oldDirName)
        assertEquals(".reaper-staging", config.stagingDirName)
        assertEquals(45.seconds, config.pollInterval)
        assertEquals(25.seconds, config.shutdownTimeout)
        assertNull(config.reaperEnabledToggleFile)
        assertNull(config.statsd)
        assertNull(config.elastic)
    }

    @Test
    fun `parses the absolute enabled-file override path`() {
        val config = create(
            mapOf("WORK_DIR" to "/work", "REAPER_ENABLED_FILE" to "/etc/async-tc-cleaner/enabled")
        ).getOrThrow()

        assertEquals(Paths.get("/etc/async-tc-cleaner/enabled"), config.reaperEnabledToggleFile)
    }

    @Test
    fun `rejects a relative enabled-file path`() {
        val error = create(
            mapOf("WORK_DIR" to "/work", "REAPER_ENABLED_FILE" to "rel/enabled")
        ).failureMessage()

        assertTrue(error.contains("REAPER_ENABLED_FILE"))
        assertTrue(error.contains("absolute"))
    }

    @Test
    fun `uses statsd host as fallback when fallback host is absent`() {
        val config = create(
            mapOf(
                "WORK_DIR" to "/work",
                "STATSD_HOST" to "statsd.local",
            )
        ).getOrThrow()

        assertEquals("statsd.local", config.statsd?.host)
        assertEquals("statsd.local", config.statsd?.fallbackHost)
    }

    @Test
    fun `parses elastic endpoints with uri api`() {
        val config = create(
            mapOf(
                "WORK_DIR" to "/work",
                "ELASTIC_ENDPOINTS" to "https://one.example, https://two.example",
                "ELASTIC_INDEX" to "index-name",
                "ELASTIC_API_KEY" to "secret",
            )
        ).getOrThrow()

        assertEquals(2, config.elastic?.endpoints?.size)
        assertEquals("index-name", config.elastic?.index)
        assertEquals("secret", config.elastic?.apiKey)
    }

    @Test
    fun `rejects relative work dir`() {
        val error = create(mapOf("WORK_DIR" to "relative/work")).failureMessage()

        assertTrue(error.contains("WORK_DIR"))
        assertTrue(error.contains("absolute"))
    }

    @Test
    fun `rejects filesystem root work dir`() {
        val error = create(mapOf("WORK_DIR" to "/")).failureMessage()

        assertTrue(error.contains("WORK_DIR"))
        assertTrue(error.contains("root"))
    }

    @Test
    fun `reports offending numeric env name`() {
        val error = create(
            mapOf(
                "WORK_DIR" to "/work",
                "POLL_INTERVAL_SECONDS" to "soon",
            )
        ).failureMessage()

        assertTrue(error.contains("POLL_INTERVAL_SECONDS"))
        assertTrue(error.contains("soon"))
    }

    @Test
    fun `rejects values that would wrap around int truncation`() {
        // -4294966784 wraps to +512 when truncated to Int; the range check must reject it on the Long
        val error = create(
            mapOf(
                "WORK_DIR" to "/work",
                "STATSD_HOST" to "h",
                "STATSD_PORT" to "-4294966784",
            )
        ).failureMessage()

        assertTrue(error.contains("STATSD_PORT"))
        assertTrue(error.contains("-4294966784"))
    }

    @Test
    fun `rejects non-positive int`() {
        val error = create(
            mapOf(
                "WORK_DIR" to "/work",
                "STATSD_HOST" to "h",
                "STATSD_PORT" to "0",
            )
        ).failureMessage()

        assertTrue(error.contains("STATSD_PORT"))
        assertTrue(error.contains("positive"))
    }

    @Test
    fun `rejects equal old and staging dir names`() {
        val error = create(
            mapOf("WORK_DIR" to "/work", "OLD_DIR_NAME" to "x", "STAGING_DIR_NAME" to "x")
        ).failureMessage()

        assertTrue(error.contains("OLD_DIR_NAME"))
        assertTrue(error.contains("differ"))
    }

    @Test
    fun `rejects dir name with path traversal`() {
        val error = create(
            mapOf("WORK_DIR" to "/work", "OLD_DIR_NAME" to "../escape")
        ).failureMessage()

        assertTrue(error.contains("OLD_DIR_NAME"))
        assertTrue(error.contains("../escape"))
    }

    @Test
    fun `rejects negative durations`() {
        val error = create(
            mapOf(
                "WORK_DIR" to "/work",
                "POLL_INTERVAL_SECONDS" to "-1",
            )
        ).failureMessage()

        assertTrue(error.contains("POLL_INTERVAL_SECONDS"))
    }

    private fun create(env: Map<String, String>): Result<Config> {
        return EnvBasedConfigFactory(env::get).create()
    }

    private fun Result<Config>.failureMessage(): String {
        return (this as Result.Failure).throwable.message.orEmpty()
    }
}
