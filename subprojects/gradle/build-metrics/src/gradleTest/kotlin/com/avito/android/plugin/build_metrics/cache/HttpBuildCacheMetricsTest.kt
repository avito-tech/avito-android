package com.avito.android.plugin.build_metrics.cache

import com.avito.android.plugin.build_metrics.assertHasMetric
import com.avito.android.plugin.build_metrics.assertNoMetric
import com.avito.android.stats.CountMetric
import com.avito.android.stats.StatsMetric
import com.avito.test.gradle.TestResult
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
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

    private class TestCase(
        val name: String,
        val enabled: Boolean = true,
        val loadStatus: Int,
        val storeStatus: Int,
        val assertion: (result: TestResult) -> Unit
    )

    private val cases = listOf(
        TestCase(
            name = "no errors - miss and successful store",
            loadStatus = 404,
            storeStatus = 200,
            assertion = { result ->
                result.assertNoMetric<StatsMetric>(".build.cache.errors.")
            }
        ),
        // TODO: this case is flaky
        //  If it's failed, please add info to MBS-11302
        TestCase(
            name = "load error - 500 response",
            enabled = false,
            loadStatus = 500,
            storeStatus = 200,
            assertion = { result ->
                result.assertHasMetric<CountMetric>(".build.cache.errors.load.500").also {
                    assertThat(it.delta).isEqualTo(1)
                }
            }
        ),
        // TODO: this case is flaky
        //  If it's failed, please add info to MBS-11302
        TestCase(
            name = "store error - 500 response",
            enabled = false,
            loadStatus = 404,
            storeStatus = 500,
            assertion = { result ->
                result.assertHasMetric<CountMetric>(".build.cache.errors.store.500").also {
                    assertThat(it.delta).isEqualTo(1)
                }
            }
        ),
        TestCase(
            name = "store error - unknown error",
            loadStatus = 404,
            storeStatus = invalidHttpStatus,
            assertion = { result ->
                result.assertHasMetric<CountMetric>(".build.cache.errors.store.unknown").also {
                    assertThat(it.delta).isEqualTo(1)
                }
            }
        ),
        TestCase(
            name = "load error - unknown error",
            loadStatus = invalidHttpStatus,
            storeStatus = 200,
            assertion = { result ->
                result.assertHasMetric<CountMetric>(".build.cache.errors.load.unknown").also {
                    assertThat(it.delta).isEqualTo(1)
                }
            }
        ),
    )

    @TestFactory
    fun `remote cache errors`(): List<DynamicTest> {
        return cases.filter { it.enabled }.map { case ->
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
}

private const val invalidHttpStatus = -1
