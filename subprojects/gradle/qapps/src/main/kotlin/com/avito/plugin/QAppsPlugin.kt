package com.avito.plugin

import com.avito.android.withAndroidApp
import com.avito.git.gitState
import com.avito.utils.gradle.envArgs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * respects [SignServicePlugin]
 */
class QAppsPlugin : Plugin<Project> {

    private val placeholderForLocalBuild = "Local Build"

    override fun apply(target: Project) {

        // todo remove legacy extension after 2020.9
        //  change directed to remove dependency on git and env, and provide parameters from build script
        val env = target.envArgs
        val git = target.gitState()
        val legacyExtension = target.extensions.create<LegacyQAppsExtension>("qapps")

        val extension = target.extensions.create<QAppsExtension>("qappsConfig")

        @Suppress("UnstableApiUsage")
        target.withAndroidApp { appExtension ->
            appExtension.applicationVariants.all { variant ->
                target.tasks.register<QAppsUploadTask>(qappsTaskName(variant.name)) {
                    group = "ci"
                    description = "Upload ${variant.name} to qapps"

                    versionName.set(variant.versionName)
                    versionCode.set(variant.versionCode.toString())
                    packageName.set(variant.applicationId)

                    host.set(
                        extension.serviceUrl
                            .convention(legacyExtension.host)
                    )

                    comment.set(
                        extension.comment
                            .convention(getComment(env.build.number))
                    )

                    branch.set(extension.branchName
                        .convention(git.map { it.currentBranch.name }.orElse(placeholderForLocalBuild))
                    )
                }
            }
        }
    }

    private fun getComment(buildNumber: String?) =
        if (!buildNumber.isNullOrBlank()) "Teamcity build number: $buildNumber" else placeholderForLocalBuild
}
