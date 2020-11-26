package com.avito.android.build_verdict

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.module.AndroidAppModule
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import com.avito.android.build_verdict.internal.writer.RawBuildVerdictWriter.Companion.buildVerdictFileName as rawBuildVerdictFileName

class BuildVerdictPluginTest {

    @field:TempDir
    lateinit var temp: File

    private val buildVerdict by lazy { File(temp, "outputs/build-verdict/$rawBuildVerdictFileName") }
    private val gson = GsonBuilder().create()

    @Test
    fun `kotlin compilation fails - build-verdict contains kotlin compile info`() {
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
            expectedErrorLines = listOf(
                "$temp/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration",
                "$temp/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration"
            )
        )
    }

    @Test
    fun `kapt stubs generating fails - build-verdict contains kapt info`() {
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
            expectedErrorLines = listOf(
                "$temp/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration",
                "$temp/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration"
            )
        )
    }

    @Test
    fun `kapt fails - build-verdict contains kapt info`() {
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

        @Suppress("MaxLineLength")
        assertBuildVerdict(
            failedTask = "kaptDebugKotlin",
            expectedErrorLines = listOf(
                "$temp/app/build/tmp/kapt3/stubs/debug/DaggerComponent.java:6: error: [Dagger/MissingBinding] CoffeeMaker cannot be provided without an @Inject constructor or an @Provides-annotated method.",
                "public abstract interface DaggerComponent {",
                "                ^",
                "      CoffeeMaker is requested at",
                "          DaggerComponent.maker()"
            )
        )
    }

    @Test
    fun `unit test fails - build-verdict contains test info`() {
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
            expectedErrorLines = listOf(
                "FAILED tests:",
                "\tAppTest.test assert true",
                "\tAppTest.test runtime exception"
            )
        )
    }

    @Test
    fun `build success - no verdict file`() {
        generateProject()

        val result = gradlew(
            temp,
            ":app:assembleDebug",
            expectFailure = false
        )

        result.assertThat()
            .buildSuccessful()

        assertBuildVerdictFileExist(false)
    }

    @Test
    fun `buildVerdictTask fails - build-verdict contains task's verdict info`() {
        //language=Groovy
        generateProject(
            buildGradleExtra = """
                import com.avito.android.build_verdict.BuildVerdictTask
                
                class CustomTask extends DefaultTask implements BuildVerdictTask {
                
                    private verdict = null
                    
                    @Override
                    String getVerdict() {
                        return verdict
                    }
                    
                    @TaskAction 
                    void sayGreeting() {
                        verdict = "Custom verdict"
                        throw new RuntimeException("Surprise") 
                    }
                }
                
                tasks.register("customTask", CustomTask)
            """.trimIndent()
        )

        val result = gradlew(
            temp,
            ":app:customTask",
            expectFailure = true
        )

        result
            .assertThat()
            .buildFailed()

        assertBuildVerdict(
            failedTask = "customTask",
            expectedErrorLines = listOf(
                "Custom verdict"
            )
        )
    }

    private fun generateProject(
        enableKapt: Boolean = false,
        buildGradleExtra: String = "",
        mutator: File.(AndroidAppModule) -> Unit = {}
    ) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.build-verdict"),
            modules = listOf(
                AndroidAppModule(
                    name = appName,
                    enableKapt = enableKapt,
                    buildGradleExtra = buildGradleExtra,
                    mutator = mutator
                )
            )
        ).generateIn(temp)
    }

    private fun assertBuildVerdict(
        failedApp: String = appName,
        failedTask: String,
        expectedErrorLines: List<String>
    ) {
        assertBuildVerdictFileExist(true)

        val actualBuildVerdict = gson.fromJson(buildVerdict.readText(), BuildVerdict::class.java)

        assertThat(actualBuildVerdict.failedTasks)
            .hasSize(1)

        val task = actualBuildVerdict.failedTasks[0]

        assertThat(task.name)
            .isEqualTo(failedTask)

        assertThat(task.projectPath)
            .isEqualTo(":$failedApp")

        val actualErrorLines = task.errorOutput.lines()

        assertThat(actualErrorLines)
            .hasSize(expectedErrorLines.size)

        actualErrorLines.forEachIndexed { index, actualErrorLine ->
            assertThat(actualErrorLine)
                .contains(expectedErrorLines[index])
        }
    }

    private fun assertBuildVerdictFileExist(
        exist: Boolean
    ) {
        assertWithMessage("outputs/build-verdict/$rawBuildVerdictFileName exist is $exist")
            .that(buildVerdict.exists())
            .isEqualTo(exist)
    }

    companion object {
        const val appName = "app"
    }
}
