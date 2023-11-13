package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.isSuccess
import com.avito.android.plugin.build_metrics.internal.runtime.os.Cgroup2
import com.avito.android.plugin.build_metrics.internal.runtime.os.OsMetricsCollector
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class OsMetricsCollectorTest {

    @Test
    fun `send metrics`() {
        val sender = OsMetricsSenderStub()

        val result = OsMetricsCollector(Cgroup2.resolve(), sender).collect()

        Truth.assertWithMessage(result.toString())
            .that(result.isSuccess()).isTrue()

        assertThat(sender.sendInvocations).isNotEmpty()
    }
}
