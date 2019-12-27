package com.avito.android

import com.avito.utils.ExistingDirectory
import com.avito.utils.ExistingFile
import com.avito.utils.ProcessRunner
import com.avito.utils.hasContent
import org.funktionale.tries.Try
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.util.Properties

/**
 * Используется для доступа к ресурсам SDK(executables, чтение параметров) из build-скриптов
 */
@Suppress("unused")
val Project.androidHome: String
    get() = System.getenv("ANDROID_HOME")
        ?: androidHomeFromLocalProperties(rootProject.file("local.properties")) { logger.error(it) }
        ?: error("Can't resolve ANDROID_HOME")

val Project.androidSdk: AndroidSdk
    get() = AndroidSdk(this, ProcessRunner.Real())

class AndroidSdk(
    private val project: Project,
    processRunner: ProcessRunner,
    private val logger: Logger = project.logger
) : ApkSigner, Aapt {

    @Suppress("unused")
    private val targetSdkVersion: Int
        get() = requireNotNull(System.getProperty("targetSdkVersion").toIntOrNull()) { "targetSdkVersion property should be set" }

    private val compileSdkVersion: Int
        get() = requireNotNull(System.getProperty("compileSdkVersion").toIntOrNull()) { "compileSdkVersion property should be set" }


    private val buildToolsVersion: String
        get() {
            val value = System.getProperty("buildToolsVersion")
            require(value.hasContent()) { "buildToolsVersion property should be set" }
            return value
        }

    private val buildToolsPath: ExistingDirectory
        get() {
            val dir = File(androidHome.dir, "/build-tools/$buildToolsVersion")
            require(dir.exists()) {
                """========= ERROR =========
                    Android Build tools are not found in ${dir.path}
                    Please install it or update.
                """.trimIndent()
            }
            return ExistingDirectory.Impl(dir)
        }

    private val apkSigner = ApkSigner.Impl(buildToolsPath, processRunner)

    private val aapt = Aapt.Impl(buildToolsPath, processRunner)

    private val keytool = KeyTool(processRunner)

    val androidHome: ExistingDirectory
        get() = ExistingDirectory.Impl(File(System.getenv("ANDROID_HOME")
            ?: androidHomeFromLocalProperties(project.rootProject.file("local.properties")) {
                logger.error(it)
            }
            ?: error("Can't resolve ANDROID_HOME")))

    val androidJar: File
        get() {
            val file = File(androidHome.dir, "platforms/android-$compileSdkVersion/android.jar")
            require(file.exists()) {
                """========= ERROR =========
                    Android SDK tools are not found in ${file.path}
                    Please install it or update.
                """.trimIndent()
            }
            return file
        }

    override fun getApkSha1(apk: ExistingFile): Try<String> = apkSigner.getApkSha1(apk)

    override fun getPackageName(apk: File): Try<String> = aapt.getPackageName(apk)

    fun getBundleSha1(aab: ExistingFile): Try<String> = keytool.getJarSha1(aab)
}

/**
 * Используем в тестах, когда по какой-то причине не удалось зарезолвить env ANDROID_HOME
 * Нужно только в тех местах где нет доступа к project, в нашем случае это на этапе создания android проекта для
 * Gradle Test kit
 *
 * Все равно остается вариант при котором тесты не заработают:
 * local.properties первый раз генерируется во время открытия проекта avito-android в Android Studio
 * и не хранится под version control, поэтому если у разработчика не установлена ANDROID_HOME и он открывает
 * напрямую проект buildSrc в любой IDE, его ждет ошибка про "no sdk found"
 *
 * @return path до Android Sdk если получилось найти таким способом
 */
fun androidHomeFromLocalPropertiesFallback(): String {
    // не имеет смысла использовать fallback если переменная доступна
    val env = System.getenv("ANDROID_HOME")
    if (!env.isNullOrBlank()) {
        return env
    }

    val rootDir = System.getProperty("rootDir")
    val userDir = System.getProperty("user.dir")

    val avitoAndroidRoot = if (!rootDir.isNullOrBlank()) {
        // пытаемся прокинуть gradle project.rootDir
        rootDir.substringBefore("buildSrc")
    } else if (!userDir.isNullOrBlank()) {
        // IDE прокидывает сюда Working directory
        userDir.substringBefore("buildSrc")
    } else {
        null
    }

    return androidHomeFromLocalProperties(File("$avitoAndroidRoot/local.properties"))
        ?: error("Can't resolve android sdk: env ANDROID_HOME and local.properties not available")
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
