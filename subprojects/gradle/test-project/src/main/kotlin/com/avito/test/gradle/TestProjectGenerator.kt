package com.avito.test.gradle

import com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION
import com.avito.android.androidHomeFromLocalPropertiesFallback
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.files.build_gradle_kts
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.Module
import com.avito.test.gradle.module.imports
import com.avito.test.gradle.plugin.PluginsSpec
import com.avito.test.gradle.plugin.plugins
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

internal val sdkVersion: Int by lazy { System.getProperty("compileSdkVersion").toInt() }
internal val buildToolsVersion: String by lazy { System.getProperty("buildToolsVersion") }
internal val kotlinVersion: String by lazy { System.getProperty("kotlinVersion") }

internal val artifactoryUrl: String? by lazy {
    try {
        val result = System.getProperty("artifactoryUrl")
        if (result.isNullOrBlank() || result == "null") {
            null
        } else {
            result
        }
    } catch (e: Throwable) {
        null
    }
}

public interface Generator {

    public fun generateIn(file: File)
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
public class TestProjectGenerator(
    override val name: String = "test-project",
    override val imports: List<String> = emptyList(),
    override val plugins: PluginsSpec = PluginsSpec(),
    override val buildGradleExtra: String = "",
    // TODO: don't share complex default values in common test fixtures. Plugin must define them implicitly!
    override val modules: List<Module> = listOf(
        AndroidAppModule(appA, dependencies = setOf(project(":$sharedModule"))),
        AndroidAppModule(appB, dependencies = setOf(project(":$sharedModule"))),
        AndroidLibModule(sharedModule),
        AndroidLibModule(independentModule)
    ),
    override val useKts: Boolean = false,
    public val localBuildCache: File? = null,
    public val androidHome: String? = null,
    /**
     * https://docs.gradle.org/current/userguide/build_environment.html#sec:configuring_jvm_memory
     * default is -Xmx512m "-XX:MaxMetaspaceSize=256m
     * bumped because of Metaspace issues
     * https://github.com/gradle/gradle/issues/10527
     */
    public val gradleProperties: Map<String, String> = mapOf(
        "org.gradle.jvmargs" to "-Xmx1g -XX:MaxMetaspaceSize=512m"
    )
) : Module {

    override val dependencies: Set<GradleDependency> = emptySet()

    override fun generateIn(file: File) {
        with(file) {
            modules.forEach { it.generateIn(file) }

            val buildGradleContent = """
                    |${imports()}
                    |${plugins()}
                    |
                    |subprojects {
                    |    ${repositories()}
                    |}
                    |$buildGradleExtra
                    """.trimMargin()

            if (useKts) {
                build_gradle_kts {
                    writeText(buildGradleContent)
                }
            } else {
                build_gradle {
                    writeText(buildGradleContent)
                }
            }

            var settingsGradleContent = """
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android.")) {
                useModule("com.android.tools.build:gradle:$ANDROID_GRADLE_PLUGIN_VERSION")
            }
        }
    }
    ${repositories()}
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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

            if (useKts) {
                file("settings.gradle.kts", settingsGradleContent)
            } else {
                file("settings.gradle", settingsGradleContent)
            }

            file(
                ".gitignore",
                """
                        .gradle/
                        *build/
                        """.trimIndent()
            )

            FileOutputStream(file("gradle.properties")).use { file ->
                Properties().run {
                    gradleProperties.forEach { (key, value) ->
                        setProperty(key, value)
                    }
                    store(file, null)
                }
            }

            FileOutputStream(file("local.properties")).use { file ->
                Properties().run {
                    setProperty("sdk.dir", androidHome ?: androidHomeFromLocalPropertiesFallback())
                    store(file, null)
                }
            }
        }

        with(file) {
            git("init --quiet")
            commit("initial_state")
        }
    }

    private fun plugins(): PluginsSpec =
        plugins {
            id("com.android.application").apply(false)
            id("org.jetbrains.kotlin.jvm").version(kotlinVersion).apply(false)
        }.plus(plugins)

    private fun generateIncludes(modules: List<Module>, prefix: String): String =
        modules.joinToString(separator = "\n") {
            "include(\"$prefix:${it.name}\")" + "\n" + generateIncludes(it.modules, "$prefix:${it.name}")
        }

    private fun repositories(): String = if (artifactoryUrl == null) {
        """
    |repositories {
    |    mavenCentral()
    |    gradlePluginPortal()
    |    google()
    |}
    """.trimMargin()
    } else {
        """
    |repositories {
    |    ${artifactoryProxyMavenRepo("Proxy for https://repo1.maven.org/maven2", "mavenCentral")}
    |    ${artifactoryProxyMavenRepo("Proxy for https://dl.google.com/dl/android/maven2/", "google-android")}
    |    ${artifactoryProxyMavenRepo("Proxy for https://plugins.gradle.org/m2/", "gradle-plugins")}
    |}
    """.trimMargin()
    }

    private fun artifactoryProxyMavenRepo(name: String, repo: String): String {
        val allowInsecure = if (useKts) {
            "isAllowInsecureProtocol = true"
        } else {
            "allowInsecureProtocol = true"
        }

        return """
        |maven {
        |    name = "$name"
        |    setUrl("$artifactoryUrl/$repo")
        |    $allowInsecure
        |}
        """.trimMargin()
    }

    public companion object {
        public const val appA: String = "appA"
        public const val appB: String = "appB"
        public const val sharedModule: String = "shared"
        public const val independentModule: String = "independent"
    }
}
