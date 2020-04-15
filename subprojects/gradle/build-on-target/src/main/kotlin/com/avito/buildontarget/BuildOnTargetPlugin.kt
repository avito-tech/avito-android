package com.avito.buildontarget

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.TestVariant
import com.avito.android.getApkFile
import com.avito.android.withAndroidApp
import com.avito.git.Branch
import com.avito.git.gitState
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.utils.gradle.envArgs
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.register
import java.io.File

class BuildOnTargetPlugin : Plugin<Project> {

    private val ciTaskGroup = "ci"

    override fun apply(project: Project) {

        val logger = project.ciLogger
        val env = project.envArgs
        val gitState = project.gitState { logger.info(it) }

        @Suppress("UnstableApiUsage")
        val targetBranch: Provider<Branch> = gitState.flatMap { state ->
            val targetBranch = state.targetBranch
            if (targetBranch != null) {
                Providers.of(targetBranch)
            } else {
                Providers.notDefined()
            }
        }

        project.withAndroidApp { appExtension ->
            appExtension.testVariants.all { testVariant: TestVariant ->
                val testedVariant: ApplicationVariant =
                    testVariant.testedVariant as ApplicationVariant

                project.tasks.register<BuildOnTargetCommitForTestTask>(buildOnTargetTaskName()) {
                    group = ciTaskGroup
                    description = "Run build on targetCommit to get apks for tests run on target branch"

                    val nestedBuildDir = File(project.projectDir, "nested-build").apply { mkdirs() }
                    val variant = testedVariant.name
                    val versionName = project.getMandatoryStringProperty("${project.name}.versionName")
                    val versionCode = project.getMandatoryIntProperty("${project.name}.versionCode")

                    this.shouldFailBuild.set(false) //todo should be configurable outside
                    this.appPath.set(project.path)
                    this.testedVariant.set(variant)
                    this.targetCommit.set(targetBranch.map { it.commit })
                    this.tempDir.set(nestedBuildDir)
                    this.versionName.set(versionName)
                    this.versionCode.set(versionCode)
                    this.repoSshUrl.set(project.getMandatoryStringProperty("avito.repo.ssh.url"))
                    this.stubForTest.set(
                        project.getBooleanProperty(
                            "stubBuildOnTargetCommit",
                            default = false
                        )
                    )

                    onlyIf { targetBranch.isPresent }
                    onlyIf { !env.isRerunDisabled }

                    this.mainApk.set(testedVariant.packageApplicationProvider
                        .map { it.getApkFile() }
                        .map { it.relativeTo(project.rootDir) }
                        .map { RegularFile { nestedBuildDir.resolve(it) } })

                    this.testApk.set(testVariant.packageApplicationProvider
                        .map { it.getApkFile() }
                        .map { it.relativeTo(project.rootDir) }
                        .map { RegularFile { nestedBuildDir.resolve(it) } })

                    outputs.doNotCacheIf("property 'avito.buildOnTarget.disableCache' is set to true") {
                        project.getBooleanProperty("avito.buildOnTarget.disableCache", default = false)
                    }
                }
            }
        }
    }
}
