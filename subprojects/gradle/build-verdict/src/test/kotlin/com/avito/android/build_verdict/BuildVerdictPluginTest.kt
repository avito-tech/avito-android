package com.avito.android.build_verdict

import com.avito.android.build_verdict.BuildFailureListener.Companion.buildVerdictFileName
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.module.AndroidAppModule
import com.google.common.truth.Truth
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildVerdictPluginTest {

    @field:TempDir
    lateinit var temp: File
    private val gson = GsonBuilder().create()

    @Test
    fun `When kotlin compilation fails Then build-verdict contains kotlin compile info`() {
        generateProject {}

        File(temp, "app/src/main/kotlin").kotlinClass("Uncompiled") { "incorrect syntax" }
        val result = gradlew(
            temp,
            "assembleDebug",
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()

        assertBuildVerdict(
            failedTask = "compileDebugKotlin",
            errorOutput = "e: /private$temp/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration\n" +
                "e: /private$temp/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration"
        )
    }

    @Test
    fun `When kapt stubs generating fails Then build-verdict contains kapt info`() {
        generateProject(enableKapt = true) {}

        File(temp, "app/src/main/kotlin").kotlinClass("Uncompiled") { "incorrect syntax" }
        val result = gradlew(
            temp,
            "assembleDebug",
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()

        assertBuildVerdict(
            failedTask = "kaptGenerateStubsDebugKotlin",
            errorOutput = "e: /private$temp/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration\n" +
                "e: /private$temp/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration"
        )
    }

    @Test
    fun `When kapt fails Then build-verdict contains kapt info`() {
        generateProject(enableKapt = true) { module ->
            dir("src/main/kotlin/") {
                kotlinClass("DaggerComponent", module.packageName) {
                    """
                        import dagger.Component
                        class CoffeeMaker
                        
                        @Component
                        interface DaggerComponent {
                          fun maker(): CoffeeMaker
                        }
                        """.trimIndent()
                }
            }
        }

        val result = gradlew(
            temp,
            "assembleDebug",
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()

        assertBuildVerdict(
            failedTask = "kaptDebugKotlin",
            errorOutput = """/private$temp/app/build/tmp/kapt3/stubs/debug/DaggerComponent.java:6: error: [Dagger/MissingBinding] CoffeeMaker cannot be provided without an @Inject constructor or an @Provides-annotated method.
public abstract interface DaggerComponent {
                ^
      CoffeeMaker is requested at
          DaggerComponent.maker()"""
        )
    }

    @Test
    fun `When unit test fails Then build-verdict contains test info`() {
        generateProject { module ->
            dir("src/test/kotlin/") {
                kotlinClass("AppTest", module.packageName) {
                    """
                        import org.junit.Test
                        import junit.framework.Assert
                        
                        class AppTest {
                        
                            @Test
                            fun `test runtime exception`() {
                                throw RuntimeException()
                            }
                            
                            @Test
                            fun `test assert true`() {
                                Assert.assertTrue(false)
                            }
                        }
                                """.trimIndent()
                }
            }
        }

        val result = gradlew(
            temp,
            ":app:testDebug",
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()

        assertBuildVerdict(
            failedTask = "testDebugUnitTest",
            errorOutput = """FAILED tests:
	AppTest.test assert true
	AppTest.test runtime exception"""
        )
    }

    fun `When lint fails Then build-verdict contains lint info`() {

    }

    fun `When instrumentation tests fail Then build-verdict contains tests info`() {

    }

    private fun generateProject(
        enableKapt: Boolean = false,
        mutator: File.(AndroidAppModule) -> Unit = {}
    ) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.build-verdict"),
            modules = listOf(
                AndroidAppModule(
                    name = appName,
                    enableKapt = enableKapt,
                    mutator = mutator
                )
            )
        ).generateIn(temp)
    }

    private fun assertBuildVerdict(
        failedApp: String = appName,
        failedTask: String,
        errorOutput: String
    ) {
        val buildVerdict = File(temp, "outputs/build-verdict/$buildVerdictFileName")

        Truth.assertWithMessage("outputs/build-verdict/$buildVerdictFileName exist")
            .that(buildVerdict.exists())
            .isTrue()

        val actualBuildVerdict = gson.fromJson(buildVerdict.readText(), BuildVerdict::class.java)

        Truth.assertThat(actualBuildVerdict.failedTasks)
            .hasSize(1)

        val task = actualBuildVerdict.failedTasks[0]

        Truth.assertThat(task.name)
            .isEqualTo(failedTask)

        Truth.assertThat(task.projectPath)
            .isEqualTo(":$failedApp")

        Truth.assertThat(task.errorOutput)
            .contains(errorOutput)
    }

    companion object {
        val appName = "app"
    }
}
