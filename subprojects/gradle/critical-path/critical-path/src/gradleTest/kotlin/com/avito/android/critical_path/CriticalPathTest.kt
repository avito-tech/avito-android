package com.avito.android.critical_path

import com.avito.android.critical_path.internal.CriticalPathReport
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * These tests check Gradle integration scenarios but not critical path logic itself.
 */
internal class CriticalPathTest {

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
                val input = objects.fileProperty()
            
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
            ${customTaskDeclaration()}
            
            tasks.register("first", CustomTask::class.java)
            
            tasks.register("second", CustomTask::class.java) {
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
            ${customTaskDeclaration()}
            
            tasks.register("first", CustomTask::class.java)
            
            tasks.register("second", CustomTask::class.java) {
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
    fun `disabled plugin - no report`() {
        setupTasks(
            enabledPlugin = false,
            buildScript = """
            ${customTaskDeclaration()}
            
            tasks.register("work", CustomTask::class.java)
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
                id("com.avito.android.critical-path")
            }
            criticalPath {
                enabled.set($enabledPlugin)
                output.set(project.layout.projectDirectory.dir("output"))
            }
            $buildScript
            """.trimIndent()
        )
    }

    private fun customTaskDeclaration(): String {
        return """
            abstract class CustomTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
            
                init {
                    outputs.upToDateWhen { false }
                }
            
                @TaskAction
                fun action() { }
            }
        """.trimIndent()
    }

    private fun calculatePath(
        vararg tasks: String,
        args: List<String> = emptyList()
    ): Set<String> {
        val result = build(*tasks, args = args)

        val resultSubject = result.assertThat()
            .buildSuccessful()

        tasks.forEach { targetTask ->
            resultSubject
                .taskWithOutcome(targetTask, TaskOutcome.SUCCESS)
        }

        val reader = CriticalPathReport(reportFile())

        return reader.read()
            .map { it.path }
            .toSet()
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
            // We don't consider task start time at all.
            // Disable parallel execution to eliminate even accidental assumptions.
            "--no-parallel",
        )
    }

    private fun reportFile(): File =
        File(projectDir, "output/critical_path.json")
}
