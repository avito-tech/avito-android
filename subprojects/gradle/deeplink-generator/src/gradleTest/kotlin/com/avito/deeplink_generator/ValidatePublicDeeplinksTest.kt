package com.avito.deeplink_generator

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ValidatePublicDeeplinksTest {

    @Test
    fun `assemble single library - validate the same links - success`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(
                    buildScriptLinks = listOf(
                        "1/feed",
                        "1/profile"
                    ),
                    codeLinks = listOf(
                        "1/feed",
                        "1/profile"
                    )
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease")
            .assertThat()
            .buildSuccessful()
            .taskWithOutcome(":lib:validateReleasePublicDeeplinks", TaskOutcome.SUCCESS)
    }

    @Test
    fun `assemble single library - build script missing links - failure`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(
                    buildScriptLinks = listOf(
                        "1/feed",
                    ),
                    codeLinks = listOf(
                        "1/feed",
                        "1/profile"
                    )
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease", expectFailure = true)
            .assertThat()
            .buildFailed()
            .taskWithOutcome(":lib:validateReleasePublicDeeplinks", TaskOutcome.FAILED)
            .outputContains("Deeplinks are marked as public in code, but not in build script")
    }

    @Test
    fun `assemble single library - code missing links - failure`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(
                    buildScriptLinks = listOf(
                        "1/feed",
                        "1/profile"
                    ),
                    codeLinks = listOf(
                        "1/feed",
                    )
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease", expectFailure = true)
            .assertThat()
            .buildFailed()
            .taskWithOutcome(":lib:validateReleasePublicDeeplinks", TaskOutcome.FAILED)
            .outputContains("Deeplinks are marked as public in buildScript, but not in a code")
    }

    private companion object {

        fun libModule(
            buildScriptLinks: List<String>,
            codeLinks: List<String>
        ) = AndroidLibModule(
            name = "lib",
            enableKotlinAndroidPlugin = false,
            plugins = plugins { id("com.avito.android.deeplink-generator") },
            imports = listOf(
                "import com.avito.deeplink_generator.ValidatePublicDeeplinksTask",
                "import java.io.File",
                "import org.gradle.api.tasks.compile.JavaCompile",
                "import com.android.build.gradle.BaseExtension"
            ),
            buildGradleExtra = """
                        deeplinkGenerator {
                           activityIntentFilterClass.set("com.avito.deeplink_generator.SomeActivity")
                           defaultScheme.set("ru.avito")
                           
                           publicDeeplinks(
                                ${buildScriptLinks.joinToString { "\"$it\"" }}
                           )
                        }
                        
                       android.libraryVariants.configureEach {
                            val variant = this
                            val variantName = variant.name.capitalize()
                            val generateDeeplinksFromCode = 
                                tasks.register("generate%sDeeplinksFromCode".format(variantName)) {
                                    val file = File(project.buildDir, "public_links.tmp")
                                    if (file.exists()) file.delete()
                                    file.parentFile.mkdirs()
                                    file.createNewFile()
                                    file.writer().use {
                                        it.write(
                                            listOf(${codeLinks.joinToString { "\"$it\"" }})
                                            .joinToString(separator = "\n")
                                        )
                                    }
                            } 
                            val validateReleaseDeeplinks = 
                                tasks.named<ValidatePublicDeeplinksTask>(
                                    "validateReleasePublicDeeplinks".format(variantName)
                                ) { 
                                    dependsOn(generateDeeplinksFromCode)
                                    publicDeeplinksFromCode.set(File(project.buildDir, "public_links.tmp"))
                            } 
                            
                            javaCompileProvider.configure { dependsOn(validateReleaseDeeplinks) }
                       } 

                    """.trimIndent(),
            useKts = true
        )
    }
}
