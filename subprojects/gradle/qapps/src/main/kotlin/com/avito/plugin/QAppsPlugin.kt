package com.avito.plugin

import com.avito.android.withAndroidApp
import com.avito.logger.GradleLoggerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class QAppsPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<QAppsExtension>("qapps")

        project.withAndroidApp { appExtension ->
            appExtension.applicationVariants.all { variant ->

                project.tasks.register<QAppsUploadTask>(qappsTaskName(variant.name)) {
                    group = "ci"
                    description = "Upload ${variant.name} to qapps"

                    versionName.set(variant.versionName)
                    versionCode.set(variant.versionCode.toString())
                    packageName.set(variant.applicationId)

                    host.set(extension.serviceUrl)
                    comment.set(extension.comment)
                    branch.set(extension.branchName)

                    loggerFactory.set(
                        GradleLoggerFactory.fromTask(
                            project = project,
                            taskName = this.name,
                            plugin = this@QAppsPlugin
                        )
                    )
                }
            }
        }
    }
}
