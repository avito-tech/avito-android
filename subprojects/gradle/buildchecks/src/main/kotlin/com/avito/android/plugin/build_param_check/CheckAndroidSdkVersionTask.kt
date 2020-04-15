package com.avito.android.plugin.build_param_check

import com.avito.android.androidSdk
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
    abstract val revision: Property<Int>

    // synthetic output just for up-to-date checks
    @OutputFile
    val output: File = File(project.buildDir, "reports/checkAndroidSdkVersionTask.out")

    @TaskAction
    fun check() {
        val localRevision = localRevision()
        val expectedRevision = revision.get()

        if (localRevision < expectedRevision) {
            throw GradleException(
                """
            ========= ERROR =========
            ${project.androidSdk.androidJar.path} has revision $localRevision but $expectedRevision expected at least
            It breaks build caching (https://issuetracker.google.com/issues/117789774)
            Please update your local Android SDK build tools and SDK Platform
            You can disable this check temporarily via "$legacyEnabledGradleProperty" gradle property 
            or in $extensionName extension.
            ========= ERROR =========
            """.trimIndent()
            )
        }
        if (localRevision > expectedRevision) {
            logger.error(
                """
            ========= ERROR =========
            ${project.androidSdk.androidJar.path} has revision $localRevision but we use $expectedRevision in CI
            It breaks build caching (https://issuetracker.google.com/issues/117789774)
            Please update android-builder image: https://avito-tech.github.io/avito-android/docs/ci/containers/#android-builder-image
            ========= ERROR =========
            """.trimIndent()
            )
        }
        output.writeText(localRevision.toString())
    }

    private fun localRevision(): Int {
        val sourceProperties: File = project.androidSdk.platformSourceProperties(compileSdkVersion.get())
        return requireNotNull(sourceProperties.loadProperties().getProperty("Pkg.Revision", null))
            .toInt()
    }

}
