package com.avito.android.build_trace.internal

import com.avito.android.trace.CompleteEvent
import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportFileAdapter
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class BuildTraceReportWriterTest {

    @Test
    fun `report prepared - writer invoked - output contains readable trace`(@TempDir tempDir: File) {
        val output = File(tempDir, "build.trace")
        val event = event(":recoveryTask")
        val writer = BuildTraceReportWriter(output)

        writer.write(TraceReport(traceEvents = listOf(event)))

        assertThat(TraceReportFileAdapter(output).read().traceEvents).containsExactly(event)
    }

    @Test
    fun `normal report written - recovery requested - output keeps normal report`(@TempDir tempDir: File) {
        val output = File(tempDir, "build.trace")
        val recoveryEvent = event(":recoveryTask")
        val normalEvent = event(":normalTask")
        val writer = BuildTraceReportWriter(output)
        writer.write(TraceReport(traceEvents = listOf(normalEvent)))

        writer.write(TraceReport(traceEvents = listOf(recoveryEvent)))

        assertThat(TraceReportFileAdapter(output).read().traceEvents).containsExactly(normalEvent)
    }

    private fun event(name: String): CompleteEvent {
        return CompleteEvent(
            timestampMicroseconds = 1,
            durationMicroseconds = 2,
            processId = "process",
            threadId = "thread",
            eventName = name,
        )
    }
}
