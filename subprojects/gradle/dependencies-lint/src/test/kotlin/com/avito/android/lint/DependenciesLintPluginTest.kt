package com.avito.android.lint

import com.avito.android.lint.report.DependenciesReport
import com.avito.android.lint.report.RedundantDependency
import com.avito.android.lint.report.ReportXmlAdapter
import com.avito.android.lint.report.UnusedDependency
import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.KotlinModule
import com.avito.test.gradle.Module
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.dir
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.kotlinClass
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.isInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class DependenciesLintPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()
    }

    @TestFactory
    fun `no issues - no dependencies at all`(): List<DynamicTest> {
        return listOf(
            "android app" to AndroidAppModule("app"),
            "android lib" to AndroidLibModule("feature"),
            "kotlin lib" to KotlinModule("lib")
        ).map { case ->
            dynamicTest(case.first) {
                val dir = dirForDynamicTest()
                generateProject(dir, case.second)

                val report = runLinter(dir)

                assertWithMessage("found issues").that(report.issues).isEmpty()
            }
        }
    }

    @TestFactory
    fun `no issues - has usage from code`(): List<DynamicTest> {
        val libraryModule = KotlinModule("lib") {
            dir("src/main/kotlin") {
                kotlinClass("LibModuleUtil")
            }
        }

        return listOf(
            "kotlin module - from implementation" to KotlinModule("feature",
                dependencies = """
                    implementation project(':lib')
                """
            ) {
                dir("src/main/kotlin") {
                    kotlinClass("Feature") {
                        """
                        val usage = LibModuleUtil::class
                        """.trimIndent()
                    }
                }
            },
            "kotlin module - from test" to KotlinModule("feature",
                dependencies = """
                    testImplementation project(':lib')
                """
            ) {
                dir("src/test/kotlin") {
                    kotlinClass("Test") {
                        """
                        val usage = LibModuleUtil::class
                        """.trimIndent()
                    }
                }
            },
            "app module - from implementation" to AndroidAppModule("app",
                dependencies = """
                    implementation project(':lib')
                """,
                plugins = listOf("kotlin-android")
            ) {
                dir("src/main/kotlin") {
                    kotlinClass("App") {
                        """
                        val usage = LibModuleUtil::class
                        """.trimIndent()
                    }
                }
            },
            "app module - from tests" to AndroidAppModule("app",
                dependencies = """
                    testImplementation project(':lib')
                """,
                plugins = listOf("kotlin-android")
            ) {
                dir("src/test/kotlin") {
                    kotlinClass("Test") {
                        """
                        val usage = LibModuleUtil::class
                        """.trimIndent()
                    }
                }
            },
            "app module - from android tests" to AndroidAppModule("app",
                dependencies = """
                    androidTestImplementation project(':lib')
                """,
                plugins = listOf("kotlin-android")
            ) {
                dir("src/androidTest/kotlin") {
                    kotlinClass("AndroidTest") {
                        """
                        val usage = LibModuleUtil::class
                        """.trimIndent()
                    }
                }
            },
            "lib module - from implementation" to AndroidLibModule("feature",
                dependencies = """
                    implementation project(':lib')
                """
            ) {
                dir("src/main/kotlin") {
                    kotlinClass("Feature") {
                        """
                        val usage = LibModuleUtil::class
                        """.trimIndent()
                    }
                }
            },
            "lib module - from tests" to AndroidLibModule("feature",
                dependencies = """
                    testImplementation project(':lib')
                """
            ) {
                dir("src/test/kotlin") {
                    kotlinClass("Test") {
                        """
                        val usage = LibModuleUtil::class
                        """.trimIndent()
                    }
                }
            },
            "lib module - from android tests" to AndroidLibModule("feature",
                dependencies = """
                    androidTestImplementation project(':lib')
                """
            ) {
                dir("src/androidTest/kotlin") {
                    kotlinClass("AndroidTest") {
                        """
                        val usage = LibModuleUtil::class
                        """.trimIndent()
                    }
                }
            }
        ).map { case ->
            dynamicTest(case.first) {
                val dir = dirForDynamicTest()
                val testableModule = case.second
                generateProject(dir, testableModule, libraryModule)

                val report = runLinter(dir)

                assertWithMessage("found issues").that(report.issues).isEmpty()
            }
        }
    }

    @TestFactory
    fun `unused binary dependency`(): List<DynamicTest> {
        return listOf(
            "kotlin module - from implementation" to KotlinModule("feature",
                dependencies = """
                    implementation "$rxJava2"
                """
            ),
            "kotlin module - from test" to KotlinModule("feature",
                dependencies = """
                    testImplementation "$rxJava2"
                """
            ),
            "app module - from implementation" to AndroidAppModule("app",
                dependencies = """
                    implementation "$rxJava2"
                """,
                plugins = listOf("kotlin-android")
            ),
            "app module - from tests" to AndroidAppModule("app",
                dependencies = """
                    testImplementation "$rxJava2"
                """,
                plugins = listOf("kotlin-android")
            ),
            "app module - from android tests" to AndroidAppModule("app",
                dependencies = """
                    androidTestImplementation "$rxJava2"
                """,
                plugins = listOf("kotlin-android")
            ),
            "lib module - from implementation" to AndroidLibModule("feature",
                dependencies = """
                    implementation "$rxJava2"
                """
            ),
            "lib module - from tests" to AndroidLibModule("feature",
                dependencies = """
                    testImplementation "$rxJava2"
                """
            ),
            "lib module - from android tests" to AndroidLibModule("feature",
                dependencies = """
                    androidTestImplementation "$rxJava2"
                """
            )
        ).map { case ->
            dynamicTest(case.first) {
                val dir = dirForDynamicTest()
                val testableModule = case.second
                generateProject(dir, testableModule)

                val report = runLinter(dir)

                assertWithMessage("found issues").that(report.issues).hasSize(1)
                val issue = report.issues.first()
                assertThat(issue).isInstanceOf<UnusedDependency>()
                issue as UnusedDependency
                assertThat(issue.message).contains("Unused dependency $rxJava2 in the project :")
            }
        }
    }

    @Test
    fun `unused module - uses only transitive dependency`() {
        generateProject(projectDir,
            AndroidAppModule("app",
                dependencies = """
                    implementation project(':lib')
                """,
                plugins = listOf("kotlin-android")
            ) {
                dir("src/main/kotlin") {
                    kotlinClass("Feature") {
                        """
                        val directUsageOfRx = io.reactivex.Observable::class
                        """.trimIndent()
                    }
                }
            },
            AndroidLibModule("lib",
                dependencies = """
                    api "$rxJava2"
                """.trimIndent()
            ) {
                dir("src/main/kotlin") {
                    kotlinClass("RxExtensions") {
                        """
                        import io.reactivex.Observable
                                
                        fun <T : Any> empty(): Observable<T> = Observable.empty()
                        """.trimIndent()
                    }
                }
            }
        )
        val report = runLinter(projectDir)

        assertWithMessage("found issues").that(report.issues).hasSize(1)
        val issue = report.issues.first()
        assertThat(issue).isInstanceOf<RedundantDependency>()
        assertThat(issue.message).contains("Unused dependency project :lib in the project :app")
        assertThat(issue.summary).contains(rxJava2)
    }

    @Test
    fun `no issues - indirect usage of transitive dependency by parent class`() {
        generateProject(projectDir,
            AndroidAppModule("app", dependencies = """
                implementation project(':feature')
                implementation project(':lib') // to resolve classes of supertypes 
                """,
                plugins = listOf("kotlin-android")
            ) {
                dir("src/test/kotlin") {
                    kotlinClass("Test") {
                        """
                        class FeatureItem : CoreItem {}
                        """.trimIndent()
                    }
                }
            },
            AndroidLibModule("feature", dependencies = """
                implementation project(':lib')
            """
            ) {
                dir("src/main/kotlin") {
                    kotlinClass("Core") {
                        """
                            interface CoreItem : LibraryItem
                        """.trimIndent()
                    }
                }
            },
            AndroidLibModule("lib") {
                dir("src/main/kotlin") {
                    kotlinClass("Library") {
                        """
                            interface LibraryItem
                        """.trimIndent()
                    }
                }
            }
        )
        val report = runLinter(projectDir)

        assertWithMessage("found issues").that(report.issues).isEmpty()
    }

    private fun generateProject(dir: File, vararg modules: Module) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact", "com.avito.android.dependencies-lint"),
            modules = modules.toList()
        ).generateIn(dir)
    }

    private fun runLinter(dir: File): DependenciesReport {
        val result = gradlew(dir, ":lintDependencies")
        result.assertThat().isInstanceOf<TestResult.Success>()

        return ReportXmlAdapter().read(
            File(dir, "build/reports/dependencies-lint.xml")
        )
    }

    /**
     * Lifecycle of dynamic test doesn't include @After.../@Before... methods
     */
    private fun dirForDynamicTest(): File {
        val prefix = "tmp"
        return Files.createTempDirectory(projectDir.toPath(), prefix).toFile()
    }

}

private const val rxJava2 = "io.reactivex.rxjava2:rxjava:2.2.11"
