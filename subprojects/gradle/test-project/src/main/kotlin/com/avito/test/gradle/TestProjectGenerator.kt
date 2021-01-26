package com.avito.test.gradle

import com.avito.android.androidHomeFromLocalPropertiesFallback
import com.avito.logger.Logger
import com.avito.logger.StubLoggerFactory
import com.avito.logger.create
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.Module
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

internal val sdkVersion: Int by lazy { System.getProperty("compileSdkVersion").toInt() }
internal val buildToolsVersion: String by lazy { System.getProperty("buildToolsVersion") }
internal val agpVersion: String by lazy { System.getProperty("androidGradlePluginVersion") }
internal val kotlinVersion: String by lazy { System.getProperty("kotlinVersion") }

interface Generator {
    fun generateIn(file: File)
}

/**
 * Кастомизируемый многомодульный проект для тестирования in-house плагинов
 * используйте TempDirectory junit 5 extension, чтобы сгенерировать временную директорию для тестов
 *
 * Структура по-умолчанию:
 * appA  appB  independent (android.library)
 *    \   /
 *   shared (android.library)
 *
 * Все наши внутренние плагины рассчитаны на наличие subprojects и не работают корректно если единственный
 * android модуль это root. Поэтому минимальный проект это один subproject модуль android application
 */
class TestProjectGenerator(
    override val name: String = "test-project",
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = "",
    // TODO: don't share complex default values in common test fixtures. Plugin must define them implicitly!
    override val modules: List<Module> = listOf(
        AndroidAppModule(appA, dependencies = setOf(project(":$sharedModule"))),
        AndroidAppModule(appB, dependencies = setOf(project(":$sharedModule"))),
        AndroidLibModule(sharedModule),
        AndroidLibModule(independentModule)
    ),
    val localBuildCache: File? = null,
    val androidHome: String? = null
) : Module {

    private val logger: Logger = StubLoggerFactory.create<TestProjectGenerator>()

    override val dependencies: Set<GradleDependency> = emptySet()

    override fun generateIn(file: File) {
        with(file) {
            modules.forEach { it.generateIn(file) }

            build_gradle {
                writeText(
                    """
        plugins {
            id 'com.android.application' apply false
            id "org.jetbrains.kotlin.jvm" version "$kotlinVersion" apply false
            ${plugins.joinToString(separator = "\n") { "id '$it'" }}
        }

        subprojects {
           repositories {
               google()
               jcenter()
           }
        }
        $buildGradleExtra
""".trimIndent()
                )
            }

            var settingsGradleContent = """
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android.")) {
                useModule("com.android.tools.build:gradle:$agpVersion")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
    }
}

rootProject.name = "${this@TestProjectGenerator.name}"

${generateIncludes(modules, "")}
            """.trimIndent()

            if (localBuildCache != null) {
                settingsGradleContent = settingsGradleContent + "\n" + """
buildCache {
    local {
        directory '${localBuildCache.toURI()}'
    }
}
                """.trimIndent()
            }

            file("settings.gradle", settingsGradleContent)

            file(
                ".gitignore",
                """
                        .gradle/
                        *build/
                        """.trimIndent()
            )

            FileOutputStream(file("local.properties")).use { file ->
                /**
                 * вместо того что писать строку, лучше доверить это properties, он например сам знает как правильно
                 * заэскейпить пути в windows
                 */
                Properties().run {
                    setProperty("sdk.dir", androidHome ?: androidHomeFromLocalPropertiesFallback(logger))
                    store(file, null)
                }
            }
        }

        with(file) {
            git("init --quiet")
            commit("initial_state")
        }
    }

    companion object {
        const val appA = "appA"
        const val appB = "appB"
        const val sharedModule = "shared"
        const val independentModule = "independent"
    }
}

private fun generateIncludes(modules: List<Module>, prefix: String): String =
    modules.joinToString(separator = "\n") {
        "include('$prefix:${it.name}')" + "\n" + generateIncludes(it.modules, "$prefix:${it.name}")
    }
