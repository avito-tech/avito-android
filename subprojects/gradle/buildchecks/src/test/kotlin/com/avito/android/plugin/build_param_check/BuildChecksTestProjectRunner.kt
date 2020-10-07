package com.avito.android.plugin.build_param_check

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import java.io.File

class BuildChecksTestProjectRunner(
    private val projectDir: File,
    private val androidHome: File? = null,
    private val buildChecksExtension: String
) {

    fun runChecks(expectFailure: Boolean = false): TestResult {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.buildchecks"),
            modules = emptyList(),
            buildGradleExtra = """
                buildChecks {
                    $buildChecksExtension
                }
            """.trimIndent(),
            androidHome = androidHome?.path
        ).generateIn(projectDir)

        val environment: Map<String, String>? = if(androidHome == null) {
            null
        } else {
            mapOf("ANDROID_HOME" to androidHome.path.toString())
        }

        return gradlew(
            projectDir,
            "help",
            //todo make params optional
            "-Pavito.stats.host=localhost",
            "-Pavito.stats.fallbackHost=localhost",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=stub",
            expectFailure = expectFailure,
            environment = environment
        )
    }
}
