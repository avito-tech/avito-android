package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.android.async_tc_cleaner.internal.config.Config
import com.avito.android.async_tc_cleaner.internal.observability.ReapObserver
import com.avito.android.async_tc_cleaner.internal.observability.ReapSweep
import com.avito.logger.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.time.TimeSource

internal class Reaper(
    private val config: Config,
    private val observer: ReapObserver,
    private val logger: Logger,
    deleter: TreeDeleter,
) {

    private val stagingDir: Path = config.workDir.resolve(config.stagingDirName)

    private val claimer = Claimer(
        oldDir = config.workDir.resolve(config.oldDirName),
        stagingDir = stagingDir,
        logger = logger,
    )

    private val deleter = StagedDeleter(
        stagingDir = stagingDir,
        deleter = deleter,
        observer = observer,
        logger = logger,
    )

    private val reaperToggle = ReaperToggle(
        toggleFile = config.reaperEnabledToggleFile,
        default = true,
        logger = logger
    )

    private var lastEnabled: Boolean? = null

    suspend fun start() {
        logger.info(
            "async-tc-cleaner started: workDir=${config.workDir}, poll=${config.pollInterval}"
        )
        try {
            while (true) {
                try {
                    runOnce()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.warn("Reap sweep failed: workDir=${config.workDir}", e)
                    observer.onSweepFailed(e)
                }
                delay(config.pollInterval)
            }
        } finally {
            logger.info("async-tc-cleaner stopped")
        }
    }

    suspend fun runOnce() {
        if (!reaperEnabled()) return
        val start = TimeSource.Monotonic.markNow()
        if (!prepareStagingDir()) return
        val claimStats = claimer.claimEligible()
        val deleteStats = stagingDir.directoryStream().fold(
            onSuccess = { stagingDirStream ->
                stagingDirStream.use { deleter.deleteAll(it) }
            },
            onFailure = { e ->
                logger.warn("Failed to open staging dir: $stagingDir", e)
                DeleteStats(
                    failed = 1,
                    outcome = DeletionOutcome(
                        0,
                        0,
                        failureSamples = listOf(FailureSamplesUtil.format(stagingDir, e)),
                    ),
                )
            }
        )
        val sweep = claimStats.toSweep(deleteStats, start.elapsedNow())
        logSweepSummary(sweep, deleteStats.failureSamples)
        observer.onSweep(sweep)
    }

    private fun reaperEnabled(): Boolean {
        val enabled = reaperToggle.isEnabled()
        if (enabled != lastEnabled) {
            lastEnabled = enabled
            logger.info(
                if (enabled) {
                    "Sweeping enabled. Toggle file: ${config.reaperEnabledToggleFile}"
                } else {
                    "Sweeping disabled via toggle: ${config.reaperEnabledToggleFile}"
                }
            )
        }
        return enabled
    }

    private fun prepareStagingDir(): Boolean {
        if (Files.exists(stagingDir, LinkOption.NOFOLLOW_LINKS) &&
            !Files.isDirectory(stagingDir, LinkOption.NOFOLLOW_LINKS)
        ) {
            logger.warn("Staging path is not a real directory (symlink or file); skipping sweep: staging=$stagingDir")
            return false
        }
        Files.createDirectories(stagingDir)
        return true
    }

    private fun logSweepSummary(sweep: ReapSweep, failureSamples: List<String>) {
        val samples = failureSamples.takeIf { it.isNotEmpty() }
            ?.let { ", failureSamples=${it.joinToString("; ")}" }
            ?: ""
        val message = "Reap sweep finished: durationMs=${sweep.durationMs}, oldDirPresent=${sweep.oldDirPresent}, " +
            "claimed=${sweep.claimed}, claimMissing=${sweep.claimMissing}, claimFailed=${sweep.claimFailed}, " +
            "unreadable=${sweep.unreadable}, deleteAttempted=${sweep.deleteAttempted}, " +
            "reaped=${sweep.reaped}, incomplete=${sweep.incomplete}, failed=${sweep.failed}, " +
            "bytesFreed=${sweep.bytesFreed}, entriesDeleted=${sweep.entriesDeleted}, " +
            "unlinkFailures=${sweep.unlinkFailures}$samples"
        if (sweep.claimFailed > 0 || sweep.unreadable > 0 || sweep.incomplete > 0 || sweep.failed > 0) {
            logger.warn(message)
        } else {
            logger.info(message)
        }
    }
}
