package com.avito.android.build_checks.internal.unique_r

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.android.build.gradle.tasks.ProcessTestManifest
import com.avito.android.androidAppExtension
import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck
import com.avito.android.build_checks.outputDirName
import com.avito.android.isAndroidApp
import com.avito.capitalize
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

internal class UniqueRClassesTaskCreator(
    private val appProject: Project,
    private val config: AndroidAppCheck.UniqueRClasses
) {

    init {
        check(appProject.isAndroidApp()) {
            "Project ${appProject.path} must be an Android app"
        }
    }

    fun addTask(rootTask: TaskProvider<Task>) {
        val appTask = registerTask(appProject)
        rootTask.dependsOn(appTask)

        appProject.androidAppExtension.applicationVariants.all { appVariant ->
            appTask.dependsOn(
                registerTask(appProject, appVariant)
            )
        }
    }

    private fun registerTask(project: Project): TaskProvider<Task> {
        return project.tasks.register<Task>(taskName) {
            group = "verification"
            description = "Verify unique R classes"
        }
    }

    private fun registerTask(
        project: Project,
        appVariant: @Suppress("DEPRECATION") com.android.build.gradle.api.ApplicationVariant
    ): TaskProvider<UniqueRClassesTask> {
        return project.tasks.register<UniqueRClassesTask>(taskName + appVariant.name.capitalize()) {
            group = "verification"
            description = "Verify unique R classes"

            allowedNonUniquePackageNames.set(config.allowedNonUniquePackageNames)

            val processAppManifest = project.tasks.withType(ProcessApplicationManifest::class.java)
                .first { it.variantName == appVariant.name }

            val processTestManifest = project.tasks.withType(ProcessTestManifest::class.java).first()

            appManifest.set(processAppManifest.mergedManifest)
            librariesManifests.set(processAppManifest.getManifests())
            testManifests.set(processTestManifest.getManifests())
            output.set(
                project.layout.buildDirectory.file("$outputDirName/$taskName/${appVariant.name}/output")
            )
        }
    }
}

private const val taskName = "checkUniqueAndroidPackages"
