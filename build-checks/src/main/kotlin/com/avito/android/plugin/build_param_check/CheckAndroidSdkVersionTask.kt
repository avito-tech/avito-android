package com.avito.android.plugin.build_param_check

import com.avito.android.androidSdk
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.utils.loadProperties
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CheckAndroidSdkVersionTask : DefaultTask() {

    @Suppress("MemberVisibilityCanBePrivate")
    @InputFile
    val sourceProperties: File = project.androidSdk.platformSourceProperties

    // synthetic output just for up-to-date checks
    @OutputFile
    val output: File = File(project.buildDir, "reports/checkAndroidSdkVersionTask.out")

    @TaskAction
    fun check() {
        val localRevision = localRevision()
        val expectedRevision = project.getMandatoryIntProperty("avito.build.androidJar.revision")

        if (localRevision < expectedRevision) {
            val message = """
            ========= ERROR =========
            ${project.androidSdk.androidJar.path} has revision $localRevision but $expectedRevision expected at least
            It breaks build caching (https://issuetracker.google.com/issues/117789774)
            Please update your local Android SDK build tools and SDK Platform
            You can disable this check temporarily via "avito.build.failOnSdkMismatch" gradle property.
            ========= ERROR =========
            """.trimIndent()

            val failOnMismatch = project.getBooleanProperty("avito.build.failOnSdkMismatch", false)
            if (failOnMismatch) {
                throw GradleException(message)
            } else {
                logger.error(message)
            }
        }
        if(localRevision > expectedRevision) {
            logger.error("""
            ========= ERROR =========
            ${project.androidSdk.androidJar.path} has revision $localRevision but we use $expectedRevision in CI
            It breaks build caching (https://issuetracker.google.com/issues/117789774)
            Please update android-builder image: http://android.k.avito.ru/ci/containers/#android-builder
            ========= ERROR =========
            """.trimIndent())
        }
        output.writeText(localRevision.toString())
    }

    private fun localRevision(): Int {
        return requireNotNull(sourceProperties.loadProperties().getProperty("Pkg.Revision", null))
            .toInt()
    }

}
