package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.JvmMetricsCollector
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.runtime.VmResolver
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.TruthJUnit.assume
import org.junit.jupiter.api.Test

internal class JvmMetricsCollectorTest {

    @Test
    fun `collect - current Gradle processes`() {
        assume().that(javaHome.isJdk).isTrue()

        val sender = JvmMetricsSenderStub()
        val collector = JvmMetricsCollector(vmResolver = VmResolver(jps), jcmd, sender)

        val result = collector.collect()

        assertWithMessage(result.toString())
            .that(result.isSuccess()).isTrue()

        val vmToUsage = sender.sendInvocations

        val gradleDaemons = vmToUsage.filter { it.first is GradleDaemon }
        assertWithMessage("Expected to get heap info for a current Gradle Daemon")
            .that(gradleDaemons).isNotEmpty()

        val gradleWorkers = vmToUsage.filter { it.first is GradleWorker }
        assertWithMessage("Expected to get heap info for a current Gradle worker")
            .that(gradleWorkers).isNotEmpty()
    }
}
