package com.avito.android.build_checks.kotlin_daemon

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class PreventKotlinDaemonFallbackTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
    }

    @Test
    fun `disabled check - uses fallback strategy`() {
        generateProject(enableCheck = false)
        val result = build(":lib1:compileKotlin")

        result.assertThat()
            .buildSuccessful()
            .outputContains("Could not connect to kotlin daemon. Using fallback strategy.")
    }

    @Test
    fun `disabled check without daemon strategy - no fallback strategy`() {
        generateProject(enableCheck = false)
        val result = build(":lib1:compileKotlin", "-Dkotlin.compiler.execution.strategy=in-process")

        result.assertThat()
            .buildSuccessful()
            .outputDoesNotContain("Could not connect to kotlin daemon. Using fallback strategy.")
    }

    @Test
    fun `single fallback - success`() {
        generateProject(enableCheck = true)
        val result = build(":lib1:compileKotlin")

        result.assertThat()
            .buildSuccessful()
    }

    @Test
    fun `multiple fallbacks - failure`() {
        generateProject(enableCheck = true)
        val result = build("compileKotlin", expectFailure = true)

        result.assertThat()
            .buildFailed()
            .outputContains("Kill Kotlin daemon")
    }

    private fun generateProject(enableCheck: Boolean) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.build-checks")
            },
            modules = listOf(
                KotlinModule(name = "lib1"),
                KotlinModule(name = "lib2"),
            ),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    preventKotlinDaemonFallback {
                        enabled = $enableCheck
                    }
                }
                """.trimIndent()
        ).generateIn(projectDir)
    }

    private fun build(
        vararg args: String,
        expectFailure: Boolean = false
    ) = gradlew(
        projectDir,
        *args,
        "-Dkotlin.daemon.jvm.options=invalid_jvm_argument_to_fail_process_startup",
        "--rerun-tasks",
        expectFailure = expectFailure
    )
}
