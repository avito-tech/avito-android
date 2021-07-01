package com.avito.android.build_trace

import com.avito.android.trace.TraceReport
import com.avito.android.trace.TraceReportFileAdapter
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class BuildTraceTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
    }

    @Test
    fun `trace - has executed task`() {
        setupTasks(
            """
            ${customTaskDeclaration()}
            
            tasks.register("customTask", CustomTask::class.java)
            """.trimIndent()
        )

        val trace = buildTrace(":customTask")

        assertWithMessage("Expected events in $trace")
            .that(trace.traceEvents).isNotEmpty()
        val taskEvent = trace.traceEvents.firstOrNull { it.eventName == ":customTask" }

        assertWithMessage("Expected event for :customTask in ${trace.traceEvents}")
            .that(taskEvent).isNotNull()

        requireNotNull(taskEvent)

        assertThat(taskEvent.args.orEmpty()["CRITICAL_PATH"]).isEqualTo(true)
    }

    @Test
    fun `disabled plugin - no trace`() {
        setupTasks(
            enabledPlugin = false,
            buildScript = """
            ${customTaskDeclaration()}
            
            tasks.register("customTask", CustomTask::class.java)
            """.trimIndent()
        )

        val result = build(":customTask")

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":customTask", TaskOutcome.SUCCESS)

        assertThat(reportFile().exists()).isFalse()
    }

    private fun setupTasks(
        buildScript: String,
        enabledPlugin: Boolean = true
    ) {
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.avito.android.build-trace")
            }
            buildTrace {
                enabled.set($enabledPlugin)
            }
            $buildScript
            """.trimIndent()
        )
    }

    private fun customTaskDeclaration(): String {
        return """
            abstract class CustomTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
            
                @Input
                val durationMs = objects.property<Long>().convention(1)
            
                @TaskAction
                fun createFile() {
                    Thread.sleep(durationMs.get())
                }
            }
        """.trimIndent()
    }

    private fun buildTrace(
        vararg tasks: String,
        args: List<String> = emptyList()
    ): TraceReport {
        val result = build(*tasks, args = args)

        val resultSubject = result.assertThat()
            .buildSuccessful()

        tasks.forEach { targetTask ->
            resultSubject
                .taskWithOutcome(targetTask, TaskOutcome.SUCCESS)
        }

        return TraceReportFileAdapter(reportFile()).read()
    }

    private fun build(
        vararg tasks: String,
        args: List<String> = emptyList()
    ): TestResult {
        return gradlew(
            projectDir,
            *tasks,
            *args.toTypedArray(),
            "--rerun-tasks",
        )
    }

    private fun reportFile(): File =
        File(projectDir, "build/reports/build-trace/build.trace")
}
