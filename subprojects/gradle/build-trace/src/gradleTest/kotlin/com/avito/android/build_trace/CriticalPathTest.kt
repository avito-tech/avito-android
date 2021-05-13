package com.avito.android.build_trace

import com.avito.android.build_trace.internal.critical_path.CriticalPathSerialization
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CriticalPathTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
    }

    @Test
    fun `input-output dependent task - path has a dependent task`() {
        setupTasks(
            """
            abstract class ProducerTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
            
                @OutputFile
                val outputFile = objects.fileProperty()
            
                @TaskAction
                fun createFile() {
                    outputFile.get().asFile.writeText("Output of a producer")
                }
            }
            
            abstract class ConsumerTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
            
                @InputFile
                var input = objects.fileProperty()
            
                @OutputFile
                val outputFile = objects.fileProperty()
            
                @TaskAction
                fun createFile() {
                    outputFile.get().asFile.writeText("Output of a consumer + " + input.get().asFile.readText())
                }
            }
            
            val producer = tasks.register("producerTask", ProducerTask::class.java) {
                outputFile.set(file("build/producer.txt"))
            }
            
            tasks.register("consumerTask", ConsumerTask::class.java) {
                input.set(producer.get().outputFile)
                outputFile.set(file("build/consumer.txt"))
            }
            """.trimIndent()
        )

        val tasks = calculatePath(":consumerTask")

        assertThat(tasks).containsExactly(":producerTask", ":consumerTask")
    }

    @Test
    fun `dependsOn dependent task - path has a dependent task`() {
        setupTasks(
            """
            ${delayTaskDeclaration()}
            
            tasks.register("first", DelayTask::class.java)
            
            tasks.register("second", DelayTask::class.java) {
                dependsOn(":first")
            }
            """.trimIndent()
        )

        val tasks = calculatePath(":second")

        assertThat(tasks).containsExactly(":first", ":second")
    }

    @Test
    fun `mustRunAfter dependent task - path has a dependent task`() {
        setupTasks(
            """
            ${delayTaskDeclaration()}
            
            tasks.register("first", DelayTask::class.java)
            
            tasks.register("second", DelayTask::class.java) {
                shouldRunAfter(":first")
            }
            """.trimIndent()
        )

        val tasks = calculatePath(":second", ":first")

        assertThat(tasks).containsExactly(":first", ":second")
    }

    @Disabled("Undefined behaviour for shouldRunAfter. It can be ignored in parallel execution")
    @Test
    fun `shouldRunAfter dependent task - no expectations`() {
        // no op
    }

    @Test
    fun `independent routes - path has the longest one`() {
        setupTasks(
            """
            ${delayTaskDeclaration()}
            
            tasks.register("short_first", DelayTask::class.java) {
                durationMs.set(1)
            }
            tasks.register("short_last", DelayTask::class.java) {
                durationMs.set(1)
                dependsOn(":short_first")
            }
            
            tasks.register("long_first", DelayTask::class.java) {
                durationMs.set(100)
            }
            tasks.register("long_last", DelayTask::class.java) {
                durationMs.set(100)
                dependsOn(":long_first")
            }
            """.trimIndent()
        )

        val tasks = calculatePath(":short_last", ":long_last")

        assertThat(tasks).containsExactly(":long_first", ":long_last")
    }

    @Test
    fun `parallel routes - path has the longest task`() {
        setupTasks(
            """
            ${delayTaskDeclaration()}
            
            tasks.register("first", DelayTask::class.java)
            
            tasks.register("intermediate_1", DelayTask::class.java){
                durationMs.set(1)
                dependsOn(":first")
            }
            tasks.register("intermediate_100", DelayTask::class.java){
                durationMs.set(100)
                dependsOn(":first")
            }
            
            tasks.register("last", DelayTask::class.java) {
                dependsOn(":intermediate_1", ":intermediate_100")
            }
            """.trimIndent()
        )
        val tasks = calculatePath(":last")

        assertThat(tasks).containsExactly(":first", ":intermediate_100", ":last")
    }

    @Test
    fun `disabled plugin - no report`() {
        setupTasks(
            enabledPlugin = false,
            buildScript = """
            ${delayTaskDeclaration()}
            
            tasks.register("work", DelayTask::class.java)
            """.trimIndent()
        )

        val result = build(":work")

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":work", TaskOutcome.SUCCESS)

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

    private fun delayTaskDeclaration(): String {
        return """
            abstract class DelayTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
            
                @Input
                var durationMs = objects.property<Long>().convention(1)
            
                @TaskAction
                fun createFile() {
                    Thread.sleep(durationMs.get())
                }
            }
        """.trimIndent()
    }

    private fun calculatePath(vararg targetTasks: String): Set<String> {
        val result = build(*targetTasks)

        val resultSubject = result.assertThat()
            .buildSuccessful()

        targetTasks.forEach { targetTask ->
            resultSubject
                .taskWithOutcome(targetTask, TaskOutcome.SUCCESS)
        }

        val reader = CriticalPathSerialization(reportFile())

        return reader.read()
            .operations
            .map { it.path }
            .toSet()
    }

    private fun build(
        vararg tasks: String,
    ): TestResult {
        return gradlew(
            projectDir,
            *tasks,
            "--rerun-tasks",
            // We don't consider task start time at all.
            // Disable parallel execution to eliminate even accidental assumptions.
            "--no-parallel",
        )
    }

    private fun reportFile(): File =
        File(projectDir, "outputs/build-trace/critical_path.json")
}
