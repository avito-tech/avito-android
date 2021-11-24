package com.avito.android.trace

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempFile

internal class TraceReportTest {

    private lateinit var tempDir: Path

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        this.tempDir = tempDir
    }

    @ExperimentalPathApi
    @Test
    fun `deserialize serialized data`() {
        val events = listOf(
            DurationEvent(
                phase = DurationEvent.PHASE_BEGIN,
                timestampMicroseconds = 0,
                processId = "process",
                threadId = "thread",
                eventName = "event",
                categories = "categories",
                color = TraceEvent.COLOR_GOOD
            ),
            DurationEvent(
                phase = DurationEvent.PHASE_END,
                timestampMicroseconds = 1,
                processId = "process",
                threadId = "thread",
                eventName = "event",
                categories = "categories",
                color = TraceEvent.COLOR_GOOD
            ),
            CompleteEvent(
                timestampMicroseconds = 1,
                durationMicroseconds = 1,
                processId = "process",
                threadId = "thread",
                eventName = "event",
                categories = "categories",
                color = TraceEvent.COLOR_GOOD
            ),
            InstantEvent(
                timestampMicroseconds = 1,
                scope = InstantEvent.SCOPE_GLOBAL,
                processId = "process",
                threadId = "thread",
                eventName = "event",
                categories = "categories",
                color = TraceEvent.COLOR_GOOD
            )
        )
        val report = TraceReport(
            traceEvents = events
        )
        val file = createTempFile(directory = tempDir).toFile()
        TraceReportFileAdapter(file).write(report)

        val deserialized = TraceReportFileAdapter(file).read()

        assertThat(deserialized).isEqualTo(report)
    }
}
