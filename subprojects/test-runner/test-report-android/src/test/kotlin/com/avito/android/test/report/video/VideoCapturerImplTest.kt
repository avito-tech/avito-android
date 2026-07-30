package com.avito.android.test.report.video

import com.avito.android.isFailure
import com.avito.android.isSuccess
import com.avito.android.stats.StubStatsdSender
import com.avito.android.stats.TimeMetric
import com.avito.logger.PrintlnLoggerFactory
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class VideoCapturerImplTest {

    @TempDir
    lateinit var tempDir: File

    private val statsDSender = StubStatsdSender()

    @Test
    fun `api 37 - video is copied from device storage`() {
        val executor = StubShellCommandExecutor(output = "video content")

        val result = capturer(executor, sdkInt = 37).run { start(); stop() }

        val video = result.getOrThrow()
        assertThat(video.readText()).isEqualTo("video content")
        assertThat(video.parentFile?.parentFile).isEqualTo(appCacheDir())
        assertThat(executor.executedCommands.last()).startsWith("cat ")
    }

    @Test
    fun `api 36 - video is copied from device storage`() {
        val executor = StubShellCommandExecutor(output = "video content")

        val result = capturer(executor, sdkInt = 36).run { start(); stop() }

        val video = result.getOrThrow()
        assertThat(video.parentFile?.parentFile).isEqualTo(appCacheDir())
        assertThat(executor.executedCommands.last()).startsWith("cat ")
    }

    @Test
    fun `api 35 - video is read from device storage as is`() {
        val executor = StubShellCommandExecutor()

        val result = capturer(executor, sdkInt = 35).run { start(); stop() }

        val video = result.getOrThrow()
        assertThat(video.parentFile).isEqualTo(reportDir())
        assertThat(executor.executedCommands.none { it.startsWith("cat ") }).isTrue()
    }

    @Test
    fun `video is not readable at first attempt - copying is retried`() {
        val executor = StubShellCommandExecutor(output = "video content")
        val validator = StubVideoValidator(readableAtAttempt = 3)

        val result = capturer(executor, sdkInt = 37, validator = validator).run { start(); stop() }

        assertThat(result.isSuccess()).isTrue()
        assertThat(validator.attempts).isEqualTo(3)
        assertThat(executor.executedCommands.count { it.startsWith("cat ") }).isEqualTo(3)
    }

    @Test
    fun `video is never readable - stop fails with not-readable metric`() {
        val executor = StubShellCommandExecutor(output = "video content")

        val result = capturer(executor, sdkInt = 37, validator = StubVideoValidator.neverReadable())
            .run { start(); stop() }

        assertThat(result.isFailure()).isTrue()
        assertThat(sentMetrics()).containsExactly("video.37.stop.error.not-readable")
    }

    @Test
    fun `shell command failed - stop fails with shell-failed metric`() {
        val executor = StubShellCommandExecutor(
            failure = IllegalStateException("no shell"),
            failWhen = { command -> command.endsWith("stop") },
        )

        val result = capturer(executor, sdkInt = 37).run { start(); stop() }

        assertThat(result.isFailure()).isTrue()
        assertThat(sentMetrics()).containsExactly("video.37.stop.error.shell-failed")
    }

    @Test
    fun `copying failed - stop fails with copy-failed metric and without retries`() {
        val executor = StubShellCommandExecutor(
            failure = IllegalStateException("no shell"),
            failWhen = { command -> command.startsWith("cat ") },
        )

        val result = capturer(executor, sdkInt = 37).run { start(); stop() }

        assertThat(result.isFailure()).isTrue()
        assertThat(executor.executedCommands.count { it.startsWith("cat ") }).isEqualTo(1)
        assertThat(sentMetrics()).containsExactly("video.37.stop.error.copy-failed")
    }

    @Test
    fun `capturer is not recording - stop fails with not-recording metric`() {
        val result = capturer(StubShellCommandExecutor(), sdkInt = 37).stop()

        assertThat(result.isFailure()).isTrue()
        assertThat(sentMetrics()).containsExactly("video.37.stop.error.not-recording")
    }

    @Test
    fun `video is saved - success metric contains duration`() {
        capturer(StubShellCommandExecutor(output = "video content"), sdkInt = 37).run { start(); stop() }

        val metric = statsDSender.getSentMetrics().single()
        assertThat(metric.name.toString()).isEqualTo("video.37.stop.success")
        assertThat((metric as TimeMetric).timeInMs).isEqualTo(STEP_MS)
    }

    private fun sentMetrics(): List<String> = statsDSender.getSentMetrics().map { it.name.toString() }

    private fun appCacheDir() = File(tempDir, "cache")

    private fun reportDir() = File(tempDir, "report")

    private fun capturer(
        executor: ShellCommandExecutor,
        sdkInt: Int,
        validator: VideoValidator = StubVideoValidator(),
    ) = VideoCapturerImpl(
        testArtifactsProvider = StubTestArtifactsProvider(reportDir()),
        appCacheDir = appCacheDir(),
        shellCommandExecutor = executor,
        videoValidator = validator,
        metrics = VideoCaptureMetrics(statsDSender, sdkInt),
        timeProvider = StubTimeProvider(stepMs = STEP_MS),
        sdkInt = sdkInt,
        loggerFactory = PrintlnLoggerFactory,
    )
}

private const val STEP_MS = 100L
