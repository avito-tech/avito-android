package com.avito.android.baseline_profile

import com.avito.git.Git
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidTestModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

object PluginTestProject {
    fun generate(
        setup: ProjectSetup = ProjectSetup(),
        useKts: Boolean = false,
        projectDirectory: File,
        git: Git,
    ) {
        val pluginsConfiguration = """
            |def artifactsOutputsDirectory = layout.buildDirectory.dir("macrobenchmark-test-outputs").get()
            |
            |applyBaselineProfile {
            |   taskConfiguration {
            |       register("${setup.pluginTaskName}") {
            |           instrumentationTaskName.set("${setup.instrumentationTaskName}")
            |           applicationModuleName.set(":${setup.appModuleName}")
            |           applicationVariantName.set("${setup.applicationVariant}")
            |           macrobenchmarksOutputDirectory.set(artifactsOutputsDirectory)
            |
            |           saveToVersionControl {
            |               enable.set(${setup.saveToVersionControl})
            |               enableRemoteOperations.set(false)
            |               includeDetailsInCommitMessage.set(false)
            |               commitMessage.set("Update baseline profile for latest code changes")
            |           }
            |       }
            |    }   
            |}
            |
            |tasks.register("${setup.instrumentationTaskName}") {
            |   doLast {
            |       artifactsOutputsDirectory.asFile.mkdirs()
            |       new File(
            |           artifactsOutputsDirectory.asFile, 
            |           "${setup.generatedProfileFileName}"
            |       ).text = "method orders... " + UUID.randomUUID() // imitate new profile at each run 
            |   }
            |}
            |
            """.trimMargin()

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = setup.appModuleName,
                    buildTypeName = setup.applicationVariant,
                ),
                AndroidTestModule(
                    name = setup.testModuleName,
                    packageName = setup.testPackageName,
                    targetApplicationModuleName = setup.appModuleName,
                    testBuildType = setup.applicationVariant,
                    plugins = plugins {
                        id("com.avito.android.apply-baseline-profile")
                    },
                    buildGradleExtra = pluginsConfiguration,
                )
            ),
            useKts = useKts
        ).generateIn(projectDirectory)

        git.checkout(branchName = setup.checkoutBranchName, create = true).getOrThrow()
    }
}
