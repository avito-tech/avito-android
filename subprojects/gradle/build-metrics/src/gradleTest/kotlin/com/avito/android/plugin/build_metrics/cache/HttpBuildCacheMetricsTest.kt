package com.avito.android.plugin.build_metrics.cache

import com.avito.android.plugin.build_metrics.assertNoMetric
import com.avito.android.plugin.build_metrics.statsdMetrics
import com.avito.android.stats.CountMetric
import com.avito.android.stats.StatsMetric
import com.avito.test.gradle.TestResult
import com.google.common.truth.Truth.assertWithMessage
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File

internal class HttpBuildCacheMetricsTest : HttpBuildCacheTestFixture() {

    private val cases = listOf(
        TestCase(
            name = "no errors - miss and successful store",
            loadStatus = 404,
            storeStatus = 200,
            assertion = { result ->
                result.assertNoMetric<StatsMetric>(".build.cache.errors.")
            }
        ),
        TestCase(
            name = "load error - 500 response",
            loadStatus = 500,
            storeStatus = 200,
            assertion = { result ->
                result.assertHasEvents(".build.cache.errors.load.500")
            }
        ),
        TestCase(
            name = "store error - 500 response",
            loadStatus = 404,
            storeStatus = 500,
            assertion = { result ->
                result.assertHasEvents(".build.cache.errors.store.500")
            }
        ),
        TestCase(
            name = "store error - unknown error",
            loadStatus = 404,
            storeStatus = invalidHttpStatus,
            assertion = { result ->
                result.assertHasEvents(".build.cache.errors.store.unknown")
            }
        ),
        TestCase(
            name = "load error - unknown error",
            loadStatus = invalidHttpStatus,
            storeStatus = 200,
            assertion = { result ->
                result.assertHasEvents(".build.cache.errors.load.unknown")
            }
        ),
    )

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

    private class TestCase(
        val name: String,
        val loadStatus: Int,
        val storeStatus: Int,
        val assertion: (result: TestResult) -> Unit
    )

    @TestFactory
    fun `remote cache errors`(): List<DynamicTest> {
        return cases.map { case ->
            DynamicTest.dynamicTest(case.name) {
                setup()

                try {
                    givenHttpBuildCache(loadHttpStatus = case.loadStatus, storeHttpStatus = case.storeStatus)

                    val result = build(":cacheMissTask")

                    result.assertThat()
                        .buildSuccessful()
                        .taskWithOutcome(":cacheMissTask", TaskOutcome.SUCCESS)

                    case.assertion(result)
                } finally {
                    cleanup()
                }
            }
        }
    }

    private fun TestResult.assertHasEvents(path: String) {
        val metrics = statsdMetrics()
        val filtered = metrics
            .filterIsInstance(CountMetric::class.java)
            .filter {
                it.name.toString().contains(path)
            }

        assertWithMessage("Expected metrics ($path) in $metrics. Logs: $output")
            .that(filtered.size).isAtLeast(1)
    }
}

private const val invalidHttpStatus = -1
