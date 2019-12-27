package com.avito.test.gradle

import com.avito.android.androidHomeFromLocalPropertiesFallback
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

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
    override val modules: List<Module> = listOf(
        AndroidAppModule(appA, dependencies = "implementation project(':$sharedModule')"),
        AndroidAppModule(appB, dependencies = "implementation project(':$sharedModule')"),
        AndroidLibModule(sharedModule),
        AndroidLibModule(independentModule)
    ),
    val localBuildCache: File? = null
) : Module {

    companion object {
        const val appA = "appA"
        const val appB = "appB"
        const val sharedModule = "shared"
        const val independentModule = "independent"
        const val transitiveModule = "transitive"

        val allModules = setOf(
            appA,
            appB,
            sharedModule,
            independentModule,
            transitiveModule
        )
    }

    override fun generateIn(file: File) {
        with(file) {
            modules.forEach { it.generateIn(file) }

            build_gradle {
                writeText(
                    """
        plugins {
            id "org.jetbrains.kotlin.jvm" version "$kotlinVersion" apply false
            ${plugins.joinToString(separator = "\n") { "id '$it'" }}
        }

        subprojects {
           repositories {
               google()
               jcenter()
           }
        }
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
                    setProperty("sdk.dir", androidHomeFromLocalPropertiesFallback())
                    store(file, null)
                }
            }
        }

        with(file) {
            git("init --quiet")
            commit("initial_state")
        }
    }
}

private fun generateIncludes(modules: List<Module>, prefix: String): String =
    modules.joinToString(separator = "\n") {
        "include('$prefix:${it.name}')" + "\n" + generateIncludes(it.modules, "$prefix:${it.name}")
    }

interface Module : Generator {
    val name: String
    val plugins: List<String>
    val buildGradleExtra: String
    val modules: List<Module>
}

interface AndroidModule : Module {
    val packageName: String
}

class KotlinModule(
    override val name: String,
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    private val dependencies: String = "",
    private val mutator: File.() -> Unit = {}
) : Module {

    override fun generateIn(file: File) {
        file.module(name) {

            build_gradle {
                writeText(
                    """
plugins {
    id 'kotlin'
    ${plugins.joinToString(separator = "\n") { "id '$it'" }}
}

$buildGradleExtra

dependencies {
    $dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
    """.trimIndent()
                )
            }
            dir("src/main") {
                dir("kotlin") {
                    kotlinClass("SomeClass")
                }
            }
            this.mutator()
        }
    }
}

/**
 * никакой полезной нагрузки, только контейнер для других модулей
 * :test:utils <- как тут test
 */
class EmptyModule(
    override val name: String,
    override val modules: List<Module>,
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = ""
) : Module {

    override fun generateIn(file: File) {
        file.module(name) {
            modules.forEach { it.generateIn(this) }
        }
    }
}

class AndroidLibModule(
    override val name: String,
    override val packageName: String = "com.$name",
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    private val dependencies: String = "",
    private val mutator: File.() -> Unit = {}
) : AndroidModule {

    override fun generateIn(file: File) {
        file.module(name) {

            build_gradle {
                writeText(
                    """
plugins {
    id 'com.android.library'
    id 'kotlin-android'
    ${plugins.joinToString(separator = "\n") { "id '$it'" }}
}

$buildGradleExtra

android {
    compileSdkVersion $sdkVersion
    buildToolsVersion "$buildToolsVersion"
}

dependencies {
    $dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
    """.trimIndent()
                )
            }
            dir("src/main") {
                androidManifest(packageName = packageName)
                dir("kotlin") {
                    kotlinClass("SomeClass")
                }
                dir("res") {
                    dir("layout") {
                        file(
                            "lib.xml", content = """<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
        android:id="@+id/title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:duplicateParentState="true"
        tools:text="Title" />
                            """.trimIndent()
                        )
                    }
                }
            }
            this.mutator()
        }
    }
}

interface Generator {
    fun generateIn(file: File)
}

class InstrumentationTest(val className: String) : Generator {
    override fun generateIn(file: File) {
        file.kotlinClass(className, content = {
            """
            import org.junit.Test

            class $className {

                @Test
                fun test() {
                    //success
                }
            }
        """.trimIndent()
        })
    }
}

class AndroidAppModule(
    override val name: String,
    override val packageName: String = "com.$name",
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    private val instrumentationTests: List<InstrumentationTest> = emptyList(),
    private val dependencies: String = "",
    private val versionName: String = "",
    private val versionCode: String = "",
    private val customScript: String = "",
    private val imports: List<String> = emptyList(),
    private val mutator: File.() -> Unit = {}
) : AndroidModule {

    override fun generateIn(file: File) {
        file.module(name) {
            dir("src") {
                dir("main") {
                    androidManifest(packageName = packageName)
                    dir("kotlin") {
                        kotlinClass("SomeClass")
                    }
                    dir("res") {
                        dir("values") {
                            file(
                                "id.xml", content = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="some_id" type="id" />
</resources>
                            """.trimIndent()
                            )
                        }
                    }
                }
                dir("androidTest") {
                    dir("java") {
                        kotlinClass("SomeClass")
                        instrumentationTests.forEach { it.generateIn(this) }
                    }
                }
            }

            file(
                "proguard.pro", """
-ignorewarnings
-keep public class * {
    public protected *;
}
""".trimIndent()
            )

            build_gradle {
                writeText(
                    """${imports.joinToString(separator = "\n")}
plugins {
    id 'com.android.application'
${plugins.joinToString(separator = "\n") { "    id '$it'" }}
}

$buildGradleExtra

android {
    compileSdkVersion $sdkVersion
    buildToolsVersion "$buildToolsVersion"
    defaultConfig {
        applicationId "$packageName"
        versionCode $versionCode
        versionName "$versionName"
    }
    buildTypes {
        release {}
        debug {}
        staging {
            initWith(debug)
            matchingFallbacks = ["debug"]
        }
    }
}

afterEvaluate{
    tasks.named("lintVitalRelease").configure { onlyIf { false } }
}

dependencies {
    $dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

$customScript
""".trimIndent()
                )
            }
            this.mutator()
        }
    }
}

val sdkVersion: Int by lazy { System.getProperty("compileSdkVersion").toInt() }
val buildToolsVersion: String by lazy { System.getProperty("buildToolsVersion") }
val agpVersion: String by lazy { System.getProperty("androidGradlePluginVersion") }
val kotlinVersion: String by lazy { System.getProperty("kotlinVersion") }

private fun File.build_gradle(configuration: File.() -> Unit = {}) = file("build.gradle").apply(configuration)

private fun File.androidManifest(packageName: String) = file(
    "AndroidManifest.xml", """
    <manifest package="$packageName"
        xmlns:android="http://schemas.android.com/apk/res/android">
    </manifest>
""".trimIndent()
)
