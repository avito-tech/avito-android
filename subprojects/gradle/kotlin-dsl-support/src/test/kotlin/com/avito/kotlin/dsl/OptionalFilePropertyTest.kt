package com.avito.kotlin.dsl

import com.google.common.truth.Truth.assertThat
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.inject.Inject

internal class OptionalFilePropertyTest {

    abstract class OptimisticTestTask : DefaultTask() {

        @get:InputFile
        @get:Optional
        abstract val someOptionalFile: RegularFileProperty

        @TaskAction
        fun doStuff() {
            val file = someOptionalFile.orNull?.asFile
            assertThat(file?.readText()).isEqualTo("bla bla bla")
        }
    }

    abstract class PessimisticTestTask : DefaultTask() {

        @get:InputFile
        @get:Optional
        abstract val someOptionalFile: RegularFileProperty

        @TaskAction
        fun doStuff() {
            val file = someOptionalFile.orNull?.asFile
            assertThat(file).isNull()
        }
    }

    abstract class PessimisticWorkerTestTask @Inject constructor(
        private val workerExecutor: WorkerExecutor
    ) : DefaultTask() {

        @get:InputFile
        @get:Optional
        abstract val someOptionalFile: RegularFileProperty

        @TaskAction
        fun doStuff() {
            workerExecutor.submit(SomeWorkerAction::class.java) { workerConfiguration ->
                workerConfiguration.isolationMode = IsolationMode.NONE
                workerConfiguration.setParams(someOptionalFile.orNull?.asFile)
            }
        }
    }

    class SomeWorkerAction @Inject constructor(private val file: File?) : Runnable {
        override fun run() {
            assertThat(file).isNull()
        }
    }

    @Test
    fun `optional file - passed to task - when non-empty file set`(@TempDir tempDir: File) {

        val project = ProjectBuilder.builder().build()

        val file = File(tempDir, "x.txt").apply {
            createNewFile()
            writeText("bla bla bla")
        }

        val task = project.tasks.register<OptimisticTestTask>("testTask") {
            someOptionalFile.set(project.optionalIfNotExists(file))
        }

        task.get().doStuff()
    }

    @Test
    fun `optional file - resolved to null in task - when nothing is set`() {

        val project = ProjectBuilder.builder().build()

        val task = project.tasks.register<PessimisticTestTask>("testTask") {}

        task.get().doStuff()
    }

    @Test
    fun `optional file - resolved to null in task - when empty file is set`() {

        val project = ProjectBuilder.builder().build()

        val nonExistentFile = File("x.txt")

        val task = project.tasks.register<PessimisticTestTask>("testTask") {
            someOptionalFile.set(project.optionalIfNotExists(nonExistentFile))
        }

        task.get().doStuff()
    }

    @Test
    fun `optional file - resolved to null in task - when empty file is set using worker api`() {

        val project = ProjectBuilder.builder().build()

        val nonExistentFile = File("x.txt")

        val task = project.tasks.register<PessimisticWorkerTestTask>("testTask") {
            someOptionalFile.set(project.optionalIfNotExists(nonExistentFile))
        }

        task.get().doStuff()
    }
}
