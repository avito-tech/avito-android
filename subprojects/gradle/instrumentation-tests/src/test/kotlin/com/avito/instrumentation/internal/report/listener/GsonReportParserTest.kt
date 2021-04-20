package com.avito.instrumentation.internal.report.listener

import com.avito.truth.ResultSubject.Companion.assertThat
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class GsonReportParserTest {

    private val parser = GsonReportParser()

    @Test
    fun `parse - failed with illegal state - empty file`(@TempDir tempDir: File) {
        val emptyFile = File(tempDir, "report.json").apply {
            createNewFile()
        }

        val result = parser.parse(emptyFile)

        assertThat(result)
            .isFailure()
            .withThrowable {
                assertThat(it).hasMessageThat().contains("Report file is empty")
            }
    }
}
