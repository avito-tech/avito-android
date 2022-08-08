package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.MemoryInfo
import com.avito.android.plugin.build_metrics.internal.runtime.OsMetricsSenderImpl
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StubStatsdSender
import com.avito.android.stats.TimeMetric
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class OsMetricsSenderTest {

    @Test
    fun `send metrics`() {
        val sender = StubStatsdSender()

        OsMetricsSenderImpl(sender).send(
            MemoryInfo(
                usedKb = 10,
                totalKb = 20
            )
        )

        val sentMetrics = sender.getSentMetrics()
        assertThat(sentMetrics).hasSize(2)

        assertThat(sentMetrics).contains(
            TimeMetric(SeriesName.create("os.memory.used", multipart = true), 10)
        )
        assertThat(sentMetrics).contains(
            TimeMetric(SeriesName.create("os.memory.total", multipart = true), 20)
        )
    }
}
