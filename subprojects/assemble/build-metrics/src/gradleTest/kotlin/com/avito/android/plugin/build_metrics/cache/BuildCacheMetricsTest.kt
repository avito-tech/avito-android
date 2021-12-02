package com.avito.android.plugin.build_metrics.cache

import com.avito.android.plugin.build_metrics.assertHasMetric
import com.avito.android.stats.CountMetric
import com.avito.test.gradle.TestResult
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import java.io.File

internal class BuildCacheMetricsTest : BuildCacheTestFixture() {

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
            tasks.register("cacheHitTask", CustomTask::class.java) {
                outputFile.set(file("build/cacheHitTask.txt"))
            }
            tasks.register("nonCacheableTask", CustomTask::class.java) {
                outputs.cacheIf { false }
                outputFile.set(file("build/nonCacheableTask.txt"))
            }
            """.trimIndent()
        )
    }

    @Test
    fun `metrics - local cache - hit`() {
        val result = warmupAndBuild(
            ":cacheHitTask",
            warmupRemote = false,
            expectedOutcome = TaskOutcome.FROM_CACHE
        )

        result.assertHasMetric<CountMetric>(".build.cache.remote.hit").also {
            assertThat(it.delta).isEqualTo(0)
        }

        result.assertHasMetric<CountMetric>(".build.cache.remote.miss").also {
            assertThat(it.delta).isEqualTo(0)
        }
    }

    @Test
    fun `metrics - local cache - miss`() {
        val result = warmupAndBuild(
            ":cacheMissTask",
            warmupRemote = false,
            expectedOutcome = TaskOutcome.SUCCESS
        )

        result.assertHasMetric<CountMetric>(".build.cache.remote.hit").also {
            assertThat(it.delta).isEqualTo(0)
        }

        result.assertHasMetric<CountMetric>(".build.cache.remote.miss").also {
            assertThat(it.delta).isEqualTo(1)
        }
    }

    @Test
    fun `metrics - local cache - non cacheable`() {
        val result = warmupAndBuild(
            ":nonCacheableTask",
            warmupRemote = false,
            expectedOutcome = TaskOutcome.SUCCESS
        )

        result.assertHasMetric<CountMetric>(".build.cache.remote.hit").also {
            assertThat(it.delta).isEqualTo(0)
        }

        result.assertHasMetric<CountMetric>(".build.cache.remote.miss").also {
            assertThat(it.delta).isEqualTo(0)
        }
    }

    @Test
    fun `metrics - remote cache - hit`() {
        val result = warmupAndBuild(
            ":cacheHitTask",
            warmupLocal = false,
            expectedOutcome = TaskOutcome.FROM_CACHE
        )

        result.assertHasMetric<CountMetric>(".build.cache.remote.hit").also {
            assertThat(it.delta).isEqualTo(1)
        }

        result.assertHasMetric<CountMetric>(".build.cache.remote.miss").also {
            assertThat(it.delta).isEqualTo(0)
        }
    }

    @Test
    fun `metrics - remote cache - miss`() {
        val result = warmupAndBuild(
            ":cacheMissTask",
            warmupLocal = false,
            expectedOutcome = TaskOutcome.SUCCESS
        )

        result.assertHasMetric<CountMetric>(".build.cache.remote.hit").also {
            assertThat(it.delta).isEqualTo(0)
        }

        result.assertHasMetric<CountMetric>(".build.cache.remote.miss").also {
            assertThat(it.delta).isEqualTo(1)
        }
    }

    @Test
    fun `metrics - remote cache - non cacheable`() {
        val result = warmupAndBuild(
            ":nonCacheableTask",
            warmupLocal = false,
            expectedOutcome = TaskOutcome.SUCCESS
        )

        result.assertHasMetric<CountMetric>(".build.cache.remote.hit").also {
            assertThat(it.delta).isEqualTo(0)
        }

        result.assertHasMetric<CountMetric>(".build.cache.remote.miss").also {
            assertThat(it.delta).isEqualTo(0)
        }
    }

    private fun warmupAndBuild(
        task: String,
        warmupLocal: Boolean = true,
        warmupRemote: Boolean = true,
        expectedOutcome: TaskOutcome
    ): TestResult {
        build(task, useLocalCache = warmupLocal, useRemoteCache = warmupRemote)
        clean()

        val result = build(task, useLocalCache = true, useRemoteCache = true)

        result.assertThat().taskWithOutcome(task, expectedOutcome)

        return result
    }
}
