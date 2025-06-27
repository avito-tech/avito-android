package com.avito.cli

import com.avito.cli.Notification.Exit
import com.avito.cli.Notification.Output
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.time.Duration.Companion.seconds

class FlowCommandLineTest {

    @Test
    fun `start - executes command and returns output notifications`() = runTest {
        val command = "echo"
        val args = listOf("Hello, World!")
        val flowCommandLine = FlowCommandLine(command, args)

        val notifications = flowCommandLine.start().toList()

        assertThat(notifications).hasSize(2)
        assertThat(notifications[0]).isInstanceOf(Output::class.java)
        assertThat((notifications[0] as Output).line).isEqualTo("Hello, World!")
        assertThat(notifications[1]).isInstanceOf(Exit::class.java)
        assertThat((notifications[1] as Exit).output).contains("Hello, World!")
    }

    @Test
    fun `start - writes output to file when provided`(@TempDir tempDir: File) = runTest {
        val command = "echo"
        val args = listOf("Hello, File!")
        val flowCommandLine = FlowCommandLine(command, args)
        val outputFile = File(tempDir, "output.txt")

        val notifications = flowCommandLine.start(output = outputFile).toList()

        assertThat(notifications).hasSize(2)
        assertThat(notifications[0]).isInstanceOf(Output::class.java)
        assertThat((notifications[0] as Output).line).isEqualTo("Hello, File!")
        assertThat(notifications[1]).isInstanceOf(Exit::class.java)

        assertThat(outputFile.exists()).isTrue()
        assertThat(outputFile.readText()).contains("Hello, File!")
    }

    @Test
    fun `start - handles command with multiple output lines`() = runTest {
        val command = "sh"
        val args = listOf("-c", "echo Line1 && echo Line2 && echo Line3")
        val flowCommandLine = FlowCommandLine(command, args)

        val notifications = flowCommandLine.start().toList()

        assertThat(notifications).hasSize(4)
        assertThat(notifications[0]).isInstanceOf(Output::class.java)
        assertThat((notifications[0] as Output).line).isEqualTo("Line1")
        assertThat(notifications[1]).isInstanceOf(Output::class.java)
        assertThat((notifications[1] as Output).line).isEqualTo("Line2")
        assertThat(notifications[2]).isInstanceOf(Output::class.java)
        assertThat((notifications[2] as Output).line).isEqualTo("Line3")
        assertThat(notifications[3]).isInstanceOf(Exit::class.java)
        assertThat((notifications[3] as Exit).output).contains("Line1")
        assertThat((notifications[3] as Exit).output).contains("Line2")
        assertThat((notifications[3] as Exit).output).contains("Line3")
    }

    @Test
    fun `start - handles command failure`() = runTest {
        val command = "false"
        val args = emptyList<String>()
        val flowCommandLine = FlowCommandLine(command, args)

        var errorCaught = false
        flowCommandLine.start()
            .catch { error ->
                assertThat(error).isInstanceOf(IllegalStateException::class.java)
                assertThat(error.message).contains("Process [false] exited with non-zero code 1")
                errorCaught = true
            }
            .collect()

        assertThat(errorCaught).isTrue()
    }

    @Test
    fun `start - handles cancellation and stops underlying process`() = runTest(timeout = 5.seconds) {
        val command = "sleep"
        val args = listOf("10")

        val flowCommandLine = FlowCommandLine(command, args)
        val process = CommandLine::class.memberProperties
            .find { it.name == "process" }
            ?.apply { isAccessible = true }
            ?.get(flowCommandLine) as Process

        var errorCaught = false
        val job = launch {
            flowCommandLine.start()
                .catch { error ->
                    assertThat(error).isInstanceOf(IllegalStateException::class.java)
                    assertThat(error.message).contains("Process [sleep, 10] exited with non-zero code 143")
                    errorCaught = true
                }.collect {
                    println(it)
                }
        }

        delay(1.seconds)

        job.cancelAndJoin()

        assertThat(process.isAlive).isFalse()
        assertThat(errorCaught).isTrue()
    }
}
