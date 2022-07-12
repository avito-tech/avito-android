package com.avito.android.plugin.build_metrics.jvm

import com.avito.android.plugin.build_metrics.internal.jvm.JvmMetricsCollector
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.jvm.VmResolver
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test

internal class JvmMetricsCollectorTest {

    @Test
    fun `collect - current Gradle processes`() {
        val collector = JvmMetricsCollector(vmResolver = VmResolver(jps), jcmd)

        val result = collector.collect()

        assertWithMessage(result.toString())
            .that(result.isSuccess()).isTrue()

        val vmToUsage = result.getOrThrow()

        val gradleDaemons = vmToUsage.keys.filterIsInstance<GradleDaemon>()
        assertWithMessage("Expected to get heap info for a current Gradle Daemon")
            .that(gradleDaemons).isNotEmpty()

        val gradleWorkers = vmToUsage.keys.filterIsInstance<GradleWorker>()
        assertWithMessage("Expected to get heap info for a current Gradle worker")
            .that(gradleWorkers).isNotEmpty()
    }
}
