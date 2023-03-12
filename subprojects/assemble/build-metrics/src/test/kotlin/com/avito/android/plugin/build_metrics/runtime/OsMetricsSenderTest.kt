package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.runtime.os.MemoryInfo
import com.avito.android.plugin.build_metrics.internal.runtime.os.OsMetricsSenderImpl
import com.avito.graphite.series.SeriesName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class OsMetricsSenderTest {

    @Test
    fun `send metrics`() {
        val buildMetricSender = StubBuildMetricsSender()

        OsMetricsSenderImpl(buildMetricSender).send(
            MemoryInfo(
                usedKb = 10,
                totalKb = 20
            )
        )

        val sentMetrics = buildMetricSender.getSentGraphiteMetrics()
        assertThat(sentMetrics).hasSize(2)

        assertThat(sentMetrics).contains(
            GraphiteMetric(SeriesName.create("os.memory.used", multipart = true), "10")
        )
        assertThat(sentMetrics).contains(
            GraphiteMetric(SeriesName.create("os.memory.total", multipart = true), "20")
        )
    }
}
