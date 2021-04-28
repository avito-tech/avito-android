package com.avito.android.plugin.build_metrics.cache

import com.avito.android.plugin.build_metrics.assertHasMetric
import com.avito.android.plugin.build_metrics.assertNoMetric
import com.avito.android.stats.CountMetric
import com.avito.android.stats.StatsMetric
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import java.io.File

internal class HttpBuildCacheMetricsTest : HttpBuildCacheTestFixture() {

    override fun setupProject(projectDir: File) {
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.avito.android.build-metrics")
            }
            
            @CacheableTask
            abstract class CustomTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

                @Input
                var input: Long = 0

                @OutputFile
                val outputFile = objects.fileProperty()

                @TaskAction
                fun createFile() {
                    outputFile.get().asFile.writeText("Output of CacheableTask: " + input)
                }
            }
            
            tasks.register("cacheMissTask", CustomTask::class.java) {
                input = System.currentTimeMillis()
                outputFile.set(file("build/cacheMissTask.txt"))
            }
            """.trimIndent()
        )
    }

    @Test
    fun `no errors - miss and successful store`() {
        givenHttpBuildCache(loadHttpStatus = 404, storeHttpStatus = 200)

        val result = build(":cacheMissTask")

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":cacheMissTask", TaskOutcome.SUCCESS)

        result.assertNoMetric<StatsMetric>(".build.cache.errors.")
    }

    @Test
    fun `load error - 500 response`() {
        givenHttpBuildCache(loadHttpStatus = 500, storeHttpStatus = 200)

        val result = build(":cacheMissTask")

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":cacheMissTask", TaskOutcome.SUCCESS)

        result.assertHasMetric<CountMetric>(".build.cache.errors.load.500").also {
            assertThat(it.delta).isEqualTo(1)
        }
    }

    @Test
    fun `store error - 500 response`() {
        givenHttpBuildCache(loadHttpStatus = 404, storeHttpStatus = 500)

        val result = build(":cacheMissTask")

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":cacheMissTask", TaskOutcome.SUCCESS)

        result.assertHasMetric<CountMetric>(".build.cache.errors.store.500").also {
            assertThat(it.delta).isEqualTo(1)
        }
    }
}
