package com.avito.android.async_tc_cleaner.internal.reaper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReapStatsTest {

    @Test
    fun `failure samples accumulate but cap at five keeping the first seen`() {
        val a = DeleteStats(failed = 3, failureSamples = listOf("s1", "s2", "s3"))
        val b = DeleteStats(failed = 4, failureSamples = listOf("s4", "s5", "s6", "s7"))

        val sum = a + b

        assertEquals(7, sum.failed, "counters accumulate unbounded")
        assertEquals(listOf("s1", "s2", "s3", "s4", "s5"), sum.failureSamples, "samples cap at 5")
    }
}
