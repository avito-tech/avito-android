package com.avito.android.plugin.build_metrics.cache

import com.avito.android.plugin.build_metrics.statsdMetrics
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import java.io.File

internal class HttpBuildCacheMetricsTest : HttpBuildCacheTestFixture() {

    override fun setupProject(projectDir: File) {
        File(projectDir, "build.gradle.kts").writeText(
            """
            import kotlin.random.Random
            
            plugins {
                id("com.avito.android.build-metrics")
            }
            
            @CacheableTask
            abstract class CustomTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

                @Input
                val fileSize = 32

                @OutputFile
                val outputFile = objects.fileProperty()

                @TaskAction
                fun createFile() {
                    val random = Random(System.currentTimeMillis())
                    val content = random.nextBytes(fileSize)
                    outputFile.get().asFile.writeBytes(content)
                }
            }

            tasks.register("customTask", CustomTask::class.java) {
                outputFile.set(file("build/outputFile.bin"))
            }
        """.trimIndent()
        )
    }

    @Test
    fun `no errors - miss and successful store`() {
        givenHttpBuildCache(loadHttpStatus = 404, storeHttpStatus = 200)

        val result = build(":customTask")

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":customTask", TaskOutcome.SUCCESS)

        val errorEvents = result.statsdMetrics()
            .filter { it.name.contains("build.cache.errors") }

        assertThat(errorEvents).isEmpty()
    }

    @Test
    fun `load error - 500 response`() {
        givenHttpBuildCache(loadHttpStatus = 500, storeHttpStatus = 200)

        val result = build(":customTask")

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":customTask", TaskOutcome.SUCCESS)

        val metrics = result.statsdMetrics()
        val storeErrors = metrics
            .filter { metric ->
                metric.type == "count"
                    && metric.name.endsWith("build.cache.errors.load.500")
            }

        assertWithMessage(metrics.joinToString())
            .that(storeErrors).hasSize(1)
    }

    @Test
    fun `store error - 500 response`() {
        givenHttpBuildCache(loadHttpStatus = 404, storeHttpStatus = 500)

        val result = build(":customTask")

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":customTask", TaskOutcome.SUCCESS)

        val metrics = result.statsdMetrics()
        val storeErrors = metrics
            .filter { metric ->
                metric.type == "count"
                    && metric.name.endsWith("build.cache.errors.store.500")
            }

        assertWithMessage(metrics.joinToString())
            .that(storeErrors).hasSize(1)
    }
}
