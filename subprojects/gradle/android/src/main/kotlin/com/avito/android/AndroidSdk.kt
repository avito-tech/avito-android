package com.avito.android

import com.avito.utils.ExistingDirectory
import com.avito.utils.ProcessRunner
import org.gradle.api.Project
import java.io.File
import java.util.Properties

val Project.androidSdk: AndroidSdk
    get() = AndroidSdk(this, ProcessRunner.Real())

class AndroidSdk(
    private val project: Project,
    private val processRunner: ProcessRunner
) {

    private val buildToolsVersion: String
        get() {
            require(project.isAndroid()) { "Trying to get android.buildToolsVersion object on non-android project: ${project.path}" }
            return project.androidBaseExtension.buildToolsVersion
        }

    private val compileSdkVersion: Int
        get() {
            require(project.isAndroid()) { "Trying to get android.compileSdkVersion object on non-android project: ${project.path}" }
            return requireNotNull(
                value = project.androidBaseExtension.compileSdkVersion,
                lazyMessage = { "compileSdkVersion is null for project: ${project.path}" }).apply {
                if (this.isBlank()) {
                    error("compileSdkVersion not set for project: ${project.path}")
                }
            }.toInt()
        }

    /**
     * it's not part of android sdk, provided here for convenience
     */
    val keytool = KeyTool(processRunner)

    val androidHome: ExistingDirectory
        get() {
            val dir = System.getenv("ANDROID_HOME")
                ?: androidHomeFromLocalProperties(
                    localPropertiesLocation = project.rootProject.file("local.properties"),
                    logger = { project.logger.error(it) })
                ?: error("Can't resolve ANDROID_HOME")
            return ExistingDirectory.Impl(dir)
        }

    val aapt: Aapt
        get() = Aapt.Impl(buildToolsPath(buildToolsVersion), processRunner)

    val androidJar: File
        get() {
            val file = File(platformDir(compileSdkVersion), "android.jar")
            require(file.exists()) {
                sdkNotFoundMessage(file.path)
            }
            return file
        }

    val platformSourceProperties: File
        get() {
            val file = File(platformDir(compileSdkVersion), "source.properties")
            require(file.exists()) {
                sdkNotFoundMessage(file.path)
            }
            return file
        }

    private fun buildToolsPath(buildToolsVersion: String): ExistingDirectory {
        val dir = File(androidHome.dir, "/build-tools/$buildToolsVersion")
        require(dir.exists()) {
            """========= ERROR =========
                    Android Build tools are not found in ${dir.path}
                    Please install it or update.
                """.trimIndent()
        }
        return ExistingDirectory.Impl(dir)
    }

    private fun platformDir(compileSdkVersion: Int): File {
        return File(androidHome.dir, "platforms/android-$compileSdkVersion")
    }

    private fun sdkNotFoundMessage(message: String): String {
        return """========= ERROR =========
                  Android SDK tools are not found.
                  Please install it or update.
                  $message
                """.trimIndent()
    }
}

/**
 * Used in tests, when for any reason ANDROID_HOME environment variable was not resolved
 * Required in places where is no access to Project, mostly on Gradle Test Kit project creation
 */
fun androidHomeFromLocalPropertiesFallback(): String {
    val env = System.getenv("ANDROID_HOME")
    if (!env.isNullOrBlank()) {
        return env
    }

    val rootDir = System.getProperty("rootDir")

    return androidHomeFromLocalProperties(File("$rootDir/local.properties"))
        ?: error("Can't resolve android sdk: env ANDROID_HOME and $rootDir/local.properties is not available")
}

private fun androidHomeFromLocalProperties(
    localPropertiesLocation: File,
    logger: (String) -> Unit = {}
): String? {
    return try {
        Properties().apply { load(localPropertiesLocation.inputStream()) }.getProperty("sdk.dir")
    } catch (e: Exception) {
        logger.invoke("Can't resolve androidHome from local.properties")
        null
    }
}
