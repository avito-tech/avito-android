package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.runtime.VmResolver
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.TruthJUnit.assume
import org.junit.jupiter.api.Test

class VmResolverTest {

    @Test
    fun `found current Gradle worker process`() {
        assume().that(javaHome.isJdk).isTrue()

        val currentVmId = ProcessHandle.current().pid()

        val vms = find()

        val vm = vms.firstOrNull { it.id == currentVmId }
        assertThat(vm).isNotNull()
        vm!!
        assertThat(vm).isInstanceOf(GradleWorker::class.java)
    }

    @Test
    fun `found Gradle daemon`() {
        assume().that(javaHome.isJdk).isTrue()

        val gradleDaemonPid = ProcessHandle.current().parent().get().pid()

        val vms = find()

        val vm = vms.firstOrNull { it.id == gradleDaemonPid }
        assertThat(vm).isNotNull()
        vm!!
        assertThat(vm).isInstanceOf(GradleDaemon::class.java)
    }

    private fun find(): Set<LocalVm> =
        VmResolver(jps).localVMs().getOrThrow()
}
