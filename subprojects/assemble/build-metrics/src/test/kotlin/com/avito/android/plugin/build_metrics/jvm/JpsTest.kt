package com.avito.android.plugin.build_metrics.jvm

import com.avito.android.Result
import com.avito.android.plugin.build_metrics.internal.jvm.JavaHome
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm
import com.avito.android.plugin.build_metrics.internal.jvm.command.Jps
import com.avito.utils.ProcessRunner
import com.avito.utils.StubProcessRunner
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.TruthJUnit.assume
import org.junit.jupiter.api.Test
import java.io.File

internal class JpsTest {

    @Test
    fun `finds a process - with main class`() {
        assume().that(javaHome.isJdk).isTrue()

        val vms = parse("53139 org.gradle.launcher.daemon.bootstrap.GradleDaemon")

        assertThat(vms).hasSize(1)
        val vm = vms.first()
        assertThat(vm.id).isEqualTo(53139L)
        assertThat(vm.name).isEqualTo("org.gradle.launcher.daemon.bootstrap.GradleDaemon")
    }

    @Test
    fun `finds a process - without main class`() {
        assume().that(javaHome.isJdk).isTrue()

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
        assume().that(javaHome.isJdk).isTrue()

        val vms = parse("")

        assertThat(vms).isEmpty()
    }

    @Test
    fun `finds a process - real implementation`() {
        assume().that(javaHome.isJdk).isTrue()

        val result = jps.run()

        assertThat(result.getOrThrow()).isNotEmpty()
    }

    @Test
    fun `fails - missing JDK binary`() {
        val jps = Jps(
            processRunner = ProcessRunner.create(workingDirectory = null),
            javaHome = JavaHome(path = File("."))
        )
        val result = jps.run()

        assertThat(result.isFailure()).isTrue()
        assertThat((result as Result.Failure).throwable)
            .hasMessageThat().contains("Missing JDK binaries")
    }

    private fun parse(output: String): Set<LocalVm.Unknown> {
        val processRunner = StubProcessRunner()
        processRunner.result = Result.Success(output)

        val vms = Jps(processRunner, javaHome).run()
        assertThat(vms.isSuccess()).isTrue()

        return vms.getOrThrow()
    }
}

internal val javaHome = JavaHome()

internal val jps = Jps(
    processRunner = ProcessRunner.create(workingDirectory = null),
    javaHome
)
