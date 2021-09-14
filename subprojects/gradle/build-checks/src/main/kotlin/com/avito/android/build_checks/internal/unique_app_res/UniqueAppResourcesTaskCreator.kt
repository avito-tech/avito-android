package com.avito.android.build_checks.internal.unique_app_res

import com.android.build.gradle.internal.res.GenerateLibraryRFileTask
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.androidAppExtension
import com.avito.android.build_checks.AndroidAppChecksExtension.AndroidAppCheck
import com.avito.android.build_checks.outputDirName
import com.avito.android.isAndroidApp
import com.avito.capitalize
import com.avito.impact.configuration.internalModule
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

internal class UniqueAppResourcesTaskCreator(
    private val appProject: Project,
    private val config: AndroidAppCheck.UniqueAppResources
) {

    init {
        check(appProject.isAndroidApp()) {
            "Project ${appProject.path} must be an Android app"
        }
    }

    fun addTask(rootTask: TaskProvider<Task>) {
        val appTask = registerTask(appProject)
        rootTask.dependsOn(appTask)

        appProject.rootProject.gradle.projectsEvaluated {
            appProject.pluginManager.withPlugin("com.android.application") {
                appProject.androidAppExtension.applicationVariants.all { appVariant ->
                    appTask.dependsOn(
                        registerTask(appProject, appVariant)
                    )
                }
            }
        }
    }

    private fun registerTask(project: Project): TaskProvider<Task> {
        return project.tasks.register<Task>(taskName) {
            group = "verification"
            description = "Verify unique app resource"
        }
    }

    /**
     * todo use new agp api
     */
    @Suppress("DEPRECATION")
    private fun registerTask(
        project: Project,
        appVariant: com.android.build.gradle.api.ApplicationVariant
    ): TaskProvider<UniqueAppResourcesTask> {
        return project.tasks.register<UniqueAppResourcesTask>(taskName + appVariant.name.capitalize()) {
            group = "verification"
            description = "Verify unique app resource"

            packageAwareRFiles.set(collectRFiles(appVariant))
            ignoredResourceTypes.set(config.ignoredResourceTypes)
            ignoredResources.set(config.ignoredResources)
            output.set(
                project.layout.buildDirectory.file("$outputDirName/$taskName/${appVariant.name}/output")
            )
        }
    }

    /**
     * Analogue of [LinkApplicationAndroidResourcesTask.dependenciesFileCollection]
     * but for projects only
     *
     * todo use new agp api
     */
    @Suppress("DEPRECATION")
    private fun collectRFiles(appVariant: com.android.build.gradle.api.ApplicationVariant): FileCollection {
        val files: ConfigurableFileCollection = appProject.objects.fileCollection()

        appProject.internalModule.mainConfiguration
            .allDependencies(includeSelf = true)
            .toSet()
            .forEach {
                it.module.project.tasks.withType(GenerateLibraryRFileTask::class.java).forEach { task ->
                    if (task.variantName == appVariant.name && !task.variantName.endsWith("Test")) {
                        files.from(task.symbolsWithPackageNameOutputFile)
                    }
                }
            }

        return files
    }
}

private const val taskName = "checkUniqueResources"
