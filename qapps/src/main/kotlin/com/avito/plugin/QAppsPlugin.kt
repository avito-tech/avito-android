package com.avito.plugin

import com.avito.android.withAndroidApp
import com.avito.git.gitState
import com.avito.git.isOnReleaseBranch
import com.avito.utils.gradle.envArgs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * respects [SignServicePlugin]
 */
@Suppress("UnstableApiUsage", "unused")
class QAppsPlugin : Plugin<Project> {

    private val pluginName = "QAppsPlugin"
    private val taskGroup = "ci"
    private val placeholderForLocalBuild = "Local Build"

    override fun apply(target: Project) {

        val env = target.envArgs
        val git = target.gitState()
        val config = target.extensions.create<QAppsExtension>("qapps")

        target.withAndroidApp { appExtension ->
            appExtension.applicationVariants.all { variant ->
                target.tasks.register<QAppsUploadTask>(qappsTaskName(variant.name)) {
                    group = taskGroup
                    description = "Upload ${variant.name} to qapps"

                    versionName.set(variant.versionName)
                    versionCode.set(variant.versionCode.toString())
                    packageName.set(variant.applicationId)

                    host.set(config.host)

                    comment.set(getComment(env.buildNumber))

                    branch.set(git.map { it.currentBranch.name }.orElse(placeholderForLocalBuild))
                    releaseChain.set(git.map { it.isOnReleaseBranch }.orElse(false))
                }
            }
        }
    }

    private fun getComment(buildNumber: String?) =
        if (!buildNumber.isNullOrBlank()) "Teamcity build number: $buildNumber" else placeholderForLocalBuild
}
