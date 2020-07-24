package com.avito.android.test.report.performance

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PerformanceTestReporterTest {

    val reporter = PerformanceTestReporter()

    @Test
    fun `get json - returns null - when nothing put`() {
        assertThat(reporter.getAsJson()).isNull()
    }

    @Test
    fun `get json - returns proper - when something put`() {
        givenFilled()

        assertThat(reporter.getAsJson()).isEqualTo("[{\"key0\":0.0},{\"key1\":1.0}]")
    }

    private fun givenFilled() {
        reporter.report("key0", 0.0)
        reporter.report("key1", 1.0)
    }
}
