package com.avito.android

import com.avito.logger.GradleLoggerFactory
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.utils.ExistingDirectoryImpl
import com.avito.utils.ProcessRunner
import org.gradle.api.Project
import java.io.File
import java.util.Properties

public val Project.androidSdk: AndroidSdk
    get() = AndroidSdk.fromAndroidProject(this)

public open class BaseAndroidSdk(
    public val androidHome: File,
    protected val processRunner: ProcessRunner
) {
    /**
     * It's not a part of android sdk, provided here for convenience
     */
    public val keytool: KeyTool = KeyTool(processRunner)

    public fun buildTools(buildToolsVersion: String): File = File(androidHome, "/build-tools/$buildToolsVersion")

    public fun platform(compileSdkVersion: Int): File = File(androidHome, "platforms/android-$compileSdkVersion")
}

public class AndroidSdk(
    androidHome: File,
    processRunner: ProcessRunner,
    private val buildToolsVersion: String
) : BaseAndroidSdk(androidHome, processRunner) {

    public val aapt: Aapt
        get() = AaptImpl(ExistingDirectoryImpl(buildTools(buildToolsVersion)), processRunner)

    public companion object {

        @JvmStatic
        internal fun fromAndroidProject(project: Project): AndroidSdk {
            require(project.isAndroid()) {
                "Trying to get Android SDK on non-android project: ${project.path}"
            }
            val buildToolsVersion = project.androidBaseExtension.buildToolsVersion

            val loggerFactory = GradleLoggerFactory.fromProject(project)

            return AndroidSdk(
                androidHome = androidHome(project.rootDir, loggerFactory.create<AndroidSdk>()),
                processRunner = ProcessRunner.create(
                    workingDirectory = null
                ),
                buildToolsVersion = buildToolsVersion
            )
        }

        @JvmStatic
        public fun fromProject(
            rootDir: File,
            loggerFactory: LoggerFactory
        ): BaseAndroidSdk {
            return BaseAndroidSdk(
                androidHome = androidHome(
                    projectRootDir = rootDir,
                    logger = loggerFactory.create<AndroidSdk>()
                ),
                processRunner = ProcessRunner.create(
                    workingDirectory = null,
                )
            )
        }

        private fun androidHome(projectRootDir: File, logger: Logger): File {
            val fromEnv: String? = System.getenv("ANDROID_HOME")
            val path = if (fromEnv.isNullOrBlank()) {
                androidHomeFromLocalProperties(
                    localPropertiesLocation = File(projectRootDir, "local.properties"),
                    logger = logger
                )
                    ?: error("Can't find ANDROID_HOME")
            } else {
                fromEnv
            }

            val dir = File(path)
            require(dir.exists()) {
                "ANDROID_HOME is not found in $path"
            }
            return dir
        }
    }
}

/**
 * Used in tests, when for any reason ANDROID_HOME environment variable was not resolved
 * Required in places where is no access to Project, mostly on Gradle Test Kit project creation
 */
public fun androidHomeFromLocalPropertiesFallback(logger: Logger): String {
    val env: String? = System.getenv("ANDROID_HOME")
    if (!env.isNullOrBlank()) {
        return env
    }

    val rootDir: String? = System.getProperty("rootDir")

    return androidHomeFromLocalProperties(
        localPropertiesLocation = File("$rootDir/local.properties"),
        logger = logger
    )
        ?: error("Can't resolve android sdk: env ANDROID_HOME and $rootDir/local.properties is not available")
}

private fun androidHomeFromLocalProperties(
    localPropertiesLocation: File,
    logger: Logger
): String? {
    return try {
        Properties().apply { load(localPropertiesLocation.inputStream()) }.getProperty("sdk.dir")
    } catch (e: Exception) {
        logger.warn("Can't resolve androidHome from local.properties", e)
        null
    }
}
