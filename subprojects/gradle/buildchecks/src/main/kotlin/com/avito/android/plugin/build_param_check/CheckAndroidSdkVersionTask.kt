package com.avito.android.plugin.build_param_check

import com.avito.android.AndroidSdk
import com.avito.utils.loadProperties
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CheckAndroidSdkVersionTask : DefaultTask() {

    @get:Input
    abstract val compileSdkVersion: Property<Int>

    @get:Input
    abstract val platformRevision: Property<Int>

    // synthetic output just for up-to-date checks
    @OutputFile
    val output: File = File(project.buildDir, "reports/checkAndroidSdkVersionTask.out")

    @TaskAction
    fun check() {
        val localRevision = localRevision()
        val expectedRevision = platformRevision.get()

        if (localRevision < expectedRevision) {
            throw GradleException(
                FailedCheckMessage(
                    BuildChecksExtension::androidSdk,
                    """
                    You have an old Android SDK Platform version.
                    API level: ${compileSdkVersion.get()}, actual revision $localRevision, expected revision: $expectedRevision.
                    It breaks build caching (https://issuetracker.google.com/issues/117789774).
                    
                    Please, install or update Android SDK Platform.
                    """.trimIndent()
                ).toString()
            )
        }
        if (localRevision > expectedRevision) {
            logger.error(
                FailedCheckMessage(
                    BuildChecksExtension::androidSdk,
                    """
                    You have a newer Android SDK Platform version.
                    API level: ${compileSdkVersion.get()}, actual revision $localRevision, expected revision: $expectedRevision.
                    It breaks build caching (https://issuetracker.google.com/issues/117789774).
                    
                    Please, update it in buildChecks config or in build environment.
                    """.trimIndent()
                ).toString()
            )
        }
        output.writeText(localRevision.toString())
    }

    private fun localRevision(): Int {
        val sourceProperties: File = platformSourceProperties
        return requireNotNull(sourceProperties.loadProperties().getProperty("Pkg.Revision", null))
            .toInt()
    }

    private val platformSourceProperties: File
        get() = File(platform, "source.properties")

    private val platform: File
        get() {
            val dir = sdk().platform(compileSdkVersion.get())
            require(dir.exists()) {
                """========= ERROR =========
               Android SDK platform ${compileSdkVersion.get()} is not found in ${dir.canonicalPath}.
               Please install it or update.
                """.trimIndent()
            }
            return dir
        }

    private fun sdk() = AndroidSdk.fromProject(project)
}
