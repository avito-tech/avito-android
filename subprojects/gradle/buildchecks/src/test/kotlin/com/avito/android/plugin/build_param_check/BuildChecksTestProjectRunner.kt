package com.avito.android.plugin.build_param_check

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import java.io.File

class BuildChecksTestProjectRunner(
    private val projectDir: File,
    private val androidHome: AndroidHomeLocation = AndroidHomeLocation.Default,
    private val buildChecksExtension: String
) {

    sealed class AndroidHomeLocation {
        object Default : AndroidHomeLocation()
        object Absent : AndroidHomeLocation()
        class Custom(val dir: File) : AndroidHomeLocation()
    }

    /***
     * @param startDir the start directory for Gradle (--project-dir). Defaults to the root project directory.
     */
    fun runChecks(
        expectFailure: Boolean = false,
        disablePlugin: Boolean = false,
        startDir: String? = null
    ): TestResult {
        val androidHomePath = if (androidHome is AndroidHomeLocation.Custom) androidHome.dir.path else null

        val environment: Map<String, String>? = when (androidHome) {
            is AndroidHomeLocation.Default -> null // the build use the system environment
            is AndroidHomeLocation.Absent -> mapOf("ANDROID_HOME" to "")
            is AndroidHomeLocation.Custom -> mapOf("ANDROID_HOME" to androidHome.dir.path.toString())
        }
        TestProjectGenerator(
            plugins = listOf("com.avito.android.buildchecks"),
            modules = listOf(
                AndroidAppModule("app")
            ),
            buildGradleExtra = """
                buildChecks {
                    $buildChecksExtension
                }
            """.trimIndent(),
            androidHome = androidHomePath
        ).generateIn(projectDir)

        if (androidHome is AndroidHomeLocation.Absent) {
            // remove TestProjectGenerator fallback
            projectDir.file("local.properties").delete()
        }
        val arguments = mutableListOf(
            "help",
            //todo make params optional
            "-Pavito.stats.host=localhost",
            "-Pavito.stats.fallbackHost=localhost",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=stub"
        )
        if (disablePlugin) {
            arguments.add("-Pavito.build-checks.enabled=false")
        }
        val dir = if (startDir == null) projectDir else File(projectDir, startDir)

        return gradlew(
            dir,
            *arguments.toTypedArray(),
            expectFailure = expectFailure,
            environment = environment
        )
    }
}
