package com.avito.instrumentation.impact.util

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.KotlinModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.append
import com.avito.test.gradle.commit
import com.avito.test.gradle.dir
import com.avito.test.gradle.git
import com.avito.test.gradle.kotlinClass
import java.io.File

const val projectToChange = "application"
const val androidModuleTestDependency = "androidModuleDependency"
const val kotlinModuleTestDependency = "kotlinModuleDependency"
const val markerClass = "kotlinModule.marker.Screen"
const val markerField = "module"
private val artifactoryUrl = System.getProperty("artifactoryUrl")

private fun generateBaseStubProject(dir: File, output: File): File {
    TestProjectGenerator(
        plugins = listOf("com.avito.android.impact"),
        modules = listOf(
            AndroidAppModule(
                name = projectToChange,
                dependencies = """
                    androidTestImplementation project(':$androidModuleTestDependency')
                    androidTestImplementation project(':$kotlinModuleTestDependency')
                """.trimIndent()
            ),
            AndroidLibModule(
                name = androidModuleTestDependency,
                dependencies = "implementation project(':$kotlinModuleTestDependency')"
            ),
            KotlinModule(
                name = kotlinModuleTestDependency
            )
        )
    ).generateIn(dir)

    with(dir) {
        git("checkout -b develop")

        append(
            "$projectToChange/build.gradle",
            """
                apply plugin: 'kotlin-android'
                apply plugin: 'com.avito.android.instrumentation-test-impact-analysis'

                instrumentationTestImpactAnalysis {
                    output.set(mkdir('${output.absolutePath}'))
                    screenMarkerClass = '$markerClass'
                    screenMarkerMetadataField = '$markerField'
                }

                repositories {
                    maven { url '$artifactoryUrl/jcenter' }
                }

                dependencies {
                    androidTestImplementation "junit:junit:4.12"
                }
            """.trimIndent()
        )

        dir("$kotlinModuleTestDependency/src/main/kotlin/kotlinModule/marker") {
            kotlinClass("Screen") {
                """
                    package kotlinModule.marker

                    interface Screen {
                        val $markerField: String
                    }
                """.trimIndent()
            }
        }
    }

    return dir
}

fun generateProjectWithScreensInSingleModule(dir: File, output: File): File {
    val project = generateBaseStubProject(dir = dir, output = output)

    with(dir) {

        dir("$projectToChange/src/androidTest/kotlin/test") {
            kotlinClass("SomeScreen") {
                """
                    package test

                    import kotlinModule.marker.Screen

                    class SomeScreen : Screen {
                        override val $markerField: String = ":$projectToChange"

                        fun foo() { }
                    }
                """.trimIndent()
            }

            kotlinClass("SomeScreenAbstraction") {
                """
                    package test

                    fun callSomeScreenThroughThisMethod() {
                      SomeScreen().foo()
                    }
                """.trimIndent()
            }

            kotlinClass("OldTestClass") {
                """
                    package test

                    import org.junit.Test

                    class OldTestClass {

                        @Test
                        fun testUsedNothing() {
                        }

                        @Test
                        fun testUsedSomeScreenDirectly() {
                          SomeScreen().foo()
                        }

                        @Test
                        fun testUsedSomeScreenThroughAbstraction() {
                          callSomeScreenThroughThisMethod()
                        }

                        @Test
                        fun testUsedSomeScreenThroughLambdaAndAbstraction() {
                          Unit.let {
                            callSomeScreenThroughThisMethod()
                          }
                        }
                    }
                """.trimIndent()
            }
        }

        commit("initial_state")
    }

    return project
}

fun generateProjectWithScreensInMultiModule(dir: File, output: File): File {
    val project = generateBaseStubProject(dir = dir, output = output)

    with(dir) {
        dir("$kotlinModuleTestDependency/src/main/kotlin/kotlinModule/marker") {
            kotlinClass("ScreenFromKotlinModule") {
                """
                    package kotlinModule.marker

                    class ScreenFromKotlinModule : Screen {
                        override val $markerField: String = ":$kotlinModuleTestDependency"

                        fun foo() { }
                    }
                """.trimIndent()
            }

            kotlinClass("Screen2FromKotlinModule") {
                """
                    package kotlinModule.marker

                    class Screen2FromKotlinModule : Screen {
                        override val $markerField: String = ":$kotlinModuleTestDependency"

                        fun foo() { }
                    }
                """.trimIndent()
            }
        }

        dir("$androidModuleTestDependency/src/main/kotlin/com/androidModule/abstractions") {
            kotlinClass("AbstractionFromAndroidModule") {
                """
                    package com.androidModule.abstractions

                    import kotlinModule.marker.ScreenFromKotlinModule
                    import kotlinModule.marker.Screen2FromKotlinModule

                    fun callScreenFromAndroidModuleThroughThisMethod() {
                      ScreenFromKotlinModule().foo()
                    }

                    fun callScreen2FromAndroidModuleThroughThisMethod() {
                      Screen2FromKotlinModule().foo()
                    }
                """.trimIndent()
            }

            kotlinClass("OpenClassInAndroidModule") {
                """
                    package com.androidModule.abstractions

                    open class OpenClassInAndroidModule {
                      open fun foo() {
                        callScreenFromAndroidModuleThroughThisMethod()
                      }
                    }
                """.trimIndent()
            }
        }

        dir("$projectToChange/src/androidTest/kotlin/test") {
            kotlinClass("OpenClassImplementation") {
                """
                    package test

                    import com.androidModule.abstractions.OpenClassInAndroidModule
                    import com.androidModule.abstractions.callScreen2FromAndroidModuleThroughThisMethod

                    class OpenClassImplementation : OpenClassInAndroidModule() {
                        override fun foo() {
                          super.foo()
                          callScreen2FromAndroidModuleThroughThisMethod()
                        }
                    }
                """.trimIndent()
            }

            kotlinClass("TestClass") {
                """
                    package test

                    import org.junit.Test
                    import kotlinModule.marker.ScreenFromKotlinModule
                    import com.androidModule.abstractions.callScreenFromAndroidModuleThroughThisMethod
                    import com.androidModule.abstractions.OpenClassInAndroidModule

                    class TestClass {

                        @Test
                        fun testUsedNothing() {
                        }

                        @Test
                        fun testUsedSomeScreenDirectly() {
                          ScreenFromKotlinModule().foo()
                        }

                        @Test
                        fun testUsedSomeScreenThroughAbstraction() {
                          callScreenFromAndroidModuleThroughThisMethod()
                        }

                        @Test
                        fun testUsedSomeScreenThroughLambdaAndAbstraction() {
                          Unit.let {
                            callScreenFromAndroidModuleThroughThisMethod()
                          }
                        }

                        @Test
                        fun testUsedBothScreensThroughImplementationOfBaseClassThatUsesScreen() {
                          val value: OpenClassInAndroidModule = OpenClassImplementation()
                          value.foo()
                        }
                    }
                """.trimIndent()
            }
        }

        commit("initial_state")
    }

    return project
}
