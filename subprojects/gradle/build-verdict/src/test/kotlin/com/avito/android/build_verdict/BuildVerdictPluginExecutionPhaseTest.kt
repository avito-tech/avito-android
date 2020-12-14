package com.avito.android.build_verdict

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.test.gradle.dir
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.KotlinModule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class BuildVerdictPluginExecutionPhaseTest : BaseBuildVerdictTest() {

    @Test
    fun `kotlin compilation fails - build-verdict contains kotlin compile info`() {
        generateProject()

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
            plainTextVerdict = compileFails(temp),
            expectedErrorLogs = listOf(
                "$temp/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration",
                "$temp/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration"
            )
        )
    }

    @Test
    fun `kapt stubs generating fails - build-verdict contains kapt info`() {
        generateProject(
            AndroidAppModule(
                name = appName,
                enableKapt = true
            )
        )

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
            plainTextVerdict = kaptStubGeneratingFails(temp),
            expectedErrorLogs = listOf(
                "$temp/app/src/main/kotlin/Uncompiled.kt: (1, 1): Expecting a top level declaration",
                "$temp/app/src/main/kotlin/Uncompiled.kt: (1, 11): Expecting a top level declaration"
            )
        )
    }

    @Test
    fun `kapt fails - build-verdict contains kapt info`() {
        generateProject(
            AndroidAppModule(
                name = appName,
                enableKapt = true,
                mutator = { module ->
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
            )
        )

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
            plainTextVerdict = kaptFails(temp),
            expectedErrorLogs = listOf(
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
        generateProject(
            AndroidAppModule(
                name = appName,
                mutator = { module ->
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
            )
        )

        val result = gradlew(
            temp,
            ":app:testDebug",
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()

        assertBuildVerdict(
            failedTask = "testDebugUnitTest",
            plainTextVerdict = unitTestsFails(temp),
            expectedErrorLogs = listOf(
                "No error logs"
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
            module = KotlinModule(
                name = appName,
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
            ),
            buildGradleExtra = """
import com.avito.android.build_verdict.TaskVerdictProducer
import org.gradle.api.Task
buildVerdict {
    onTaskFailure("customTask", { "User added verdict" } as TaskVerdictProducer)
}
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
            plainTextVerdict = customTaskFails,
            expectedErrorLogs = listOf(
                "No error logs"
            )
        )
    }

    private fun assertBuildVerdict(
        failedApp: String = appName,
        failedTask: String,
        plainTextVerdict: String,
        expectedErrorLogs: List<String>
    ) {
        assertBuildVerdictFileExist(true)
        assertThat(plainTextBuildVerdict.readText()).isEqualTo(plainTextVerdict)

        val actualBuildVerdict = gson.fromJson(jsonBuildVerdict.readText(), BuildVerdict.Execution::class.java)

        assertThat(actualBuildVerdict.failedTasks)
            .hasSize(1)

        val task = actualBuildVerdict.failedTasks[0]

        assertThat(task.name)
            .isEqualTo(failedTask)

        assertThat(task.projectPath)
            .isEqualTo(":$failedApp")

        val actualErrorLines = task.errorLogs.lines()

        assertThat(actualErrorLines)
            .hasSize(expectedErrorLogs.size)

        actualErrorLines.forEachIndexed { index, actualErrorLine ->
            assertThat(actualErrorLine)
                .contains(expectedErrorLogs[index])
        }
    }
}
