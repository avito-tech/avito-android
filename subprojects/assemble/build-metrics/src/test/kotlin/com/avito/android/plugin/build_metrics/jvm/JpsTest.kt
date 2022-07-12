package com.avito.android.plugin.build_metrics.jvm

import com.avito.android.Result
import com.avito.android.plugin.build_metrics.internal.jvm.Jps
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm
import com.avito.utils.ProcessRunner
import com.avito.utils.StubProcessRunner
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class JpsTest {

    @Test
    fun `finds a process - with main class`() {
        val vms = parse("53139 org.gradle.launcher.daemon.bootstrap.GradleDaemon")

        assertThat(vms).hasSize(1)
        val vm = vms.first()
        assertThat(vm.id).isEqualTo(53139L)
        assertThat(vm.name).isEqualTo("org.gradle.launcher.daemon.bootstrap.GradleDaemon")
    }

    @Test
    fun `finds a process - without main class`() {
        // If the target JVM is started with a custom launcher,
        // the class name (or JAR file name) and the arguments to the main method will not be available.
        val vms = parse("53139")

        assertThat(vms).hasSize(1)
        val vm = vms.first()
        assertThat(vm.id).isEqualTo(53139L)
        assertThat(vm.name).isEqualTo("Unknown")
    }

    @Test
    fun `finds no process - empty output`() {
        val vms = parse("")

        assertThat(vms).isEmpty()
    }

    @Test
    fun `finds a process - real implementation`() {
        val result = jps.run()

        assertThat(result.getOrThrow()).isNotEmpty()
    }

    private fun parse(output: String): Set<LocalVm.Unknown> {
        val processRunner = StubProcessRunner()
        processRunner.result = Result.Success(output)

        val vms = Jps(processRunner).run()
        assertThat(vms.isSuccess()).isTrue()

        return vms.getOrThrow()
    }
}

internal val jps = Jps(
    processRunner = ProcessRunner.create(workingDirectory = null)
)
