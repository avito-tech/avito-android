package com.avito.android.test.report.video

import com.avito.android.stats.StatsDSender
import com.avito.android.stats.TimeMetric
import com.avito.graphite.series.SeriesName

/**
 * Video capturing lives in the app process and its failures don't affect a test verdict,
 * so without metrics a lost video stays unnoticed until someone inspects reports manually.
 *
 * Metrics are split by api: the way a video is obtained depends on the android version,
 * see [VideoCapturerImpl].
 */
internal class VideoCaptureMetrics(
    private val statsDSender: StatsDSender,
    sdkInt: Int,
) {

    private val prefix = SeriesName.create("video", sdkInt.toString())

    fun onStopSuccess(durationMs: Long) {
        statsDSender.send(TimeMetric(prefix.append("stop", "success"), durationMs))
    }

    fun onStopError(reason: Reason, durationMs: Long) {
        statsDSender.send(TimeMetric(prefix.append("stop", "error", reason.value), durationMs))
    }

    enum class Reason(val value: String) {

        /**
         * Video never became readable: screenrecord hasn't finished the file or the copy is broken
         */
        NOT_READABLE("not-readable"),

        /**
         * Can't copy a recorded video to the app internal storage
         */
        COPY_FAILED("copy-failed"),

        /**
         * Can't execute a shell command or create an artifact file
         */
        SHELL_FAILED("shell-failed"),

        /**
         * Recording hasn't been started or has been stopped already
         */
        NOT_RECORDING("not-recording"),
    }
}
