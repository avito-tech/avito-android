package com.avito.android.proguard_guard

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.avito.android.capitalizedName
import com.avito.android.isAndroidApp
import com.avito.android.proguard_guard.shadowr8.dependsOnMinificationTask
import com.avito.android.proguard_guard.task.CheckMergedConfigurationTask
import com.avito.android.proguard_guard.task.ProguardGuardTask
import com.avito.android.proguard_guard.task.UpdateLockedConfigurationTask
import com.avito.capitalize
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.create

public class ProguardGuardPlugin : Plugin<Project> {

    private val Project.pluginIsEnabled: Boolean
        get() = providers
            .gradleProperty(enabledProp)
            .map { it.toBoolean() }
            .getOrElse(true)

    private val Project.debug: Boolean
        get() = providers
            .gradleProperty(debugProp)
            .map { it.toBoolean() }
            .getOrElse(false)

    override fun apply(project: Project) {
        if (!project.pluginIsEnabled) {
            project.logger.lifecycle("ProguardGuard plugin is disabled by $enabledProp property")
            return
        }
        require(project.isAndroidApp()) {
            "Failed to apply ProguardGuard plugin because can't find com.application.android plugin. " +
                "Remove this plugin from project or apply com.application.android if this module " +
                "should be an android application module."
        }

        val extension = project.extensions.create<ProguardGuardExtension>("proguardGuard")

        val androidComponents =
            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

        extension.variantsConfiguration.all { lockedVariant ->
            androidComponents.onVariants(
                selector = androidComponents.selector().withName(lockedVariant.name)
            ) { appVariant: ApplicationVariant ->
                handleApplicationVariant(project, appVariant, lockedVariant)
            }
        }
    }

    private fun handleApplicationVariant(
        project: Project,
        variant: ApplicationVariant,
        extension: BuildVariantProguardGuardConfiguration,
    ) {
        registerCheckTask(project, variant, extension)
        registerUpdateTask(project, variant, extension)
    }

    private fun registerCheckTask(
        project: Project,
        variant: ApplicationVariant,
        extension: BuildVariantProguardGuardConfiguration,
    ) {
        val updateTaskPath = project.path + ":" + computeUpdateTaskName(variant.name)
        registerProguardGuardTask<CheckMergedConfigurationTask>(
            project = project,
            name = computeCheckTaskName(variant.name),
            variant = variant,
            shadowR8Task = extension.shadowR8Task.get(),
            taskConstructorArgs = arrayOf(updateTaskPath, project.debug)
        ) { task ->
            task.group = "Proguard guard"
            task.description = "Compare ${variant.capitalizedName()} proguard config with locked config"
            task.mergedConfigurationFile.set(extension.mergedConfigurationFile)
            task.sortedMergedConfigurationFile.setSortedFileFrom(task.mergedConfigurationFile)
            task.lockedConfigurationFile.set(extension.lockedConfigurationFile)
            task.failOnDifference.set(extension.failOnDifference)
            task.diffFile.set(extension.outputFile)
        }
    }

    private fun registerUpdateTask(
        project: Project,
        variant: ApplicationVariant,
        extension: BuildVariantProguardGuardConfiguration,
    ) {
        registerProguardGuardTask<UpdateLockedConfigurationTask>(
            project = project,
            name = computeUpdateTaskName(variant.name),
            variant = variant,
            shadowR8Task = extension.shadowR8Task.get(),
            taskConstructorArgs = arrayOf(project.debug)
        ) { task ->
            task.group = "Proguard guard"
            task.description = "Update locked proguard config with ${variant.capitalizedName()}"
            task.mergedConfigurationFile.set(extension.mergedConfigurationFile)
            task.sortedMergedConfigurationFile.setSortedFileFrom(task.mergedConfigurationFile)
            task.lockedConfigurationFile.set(extension.lockedConfigurationFile)
        }
    }

    private inline fun <reified T : ProguardGuardTask> registerProguardGuardTask(
        project: Project,
        name: String,
        variant: ApplicationVariant,
        shadowR8Task: Boolean,
        vararg taskConstructorArgs: Any,
        crossinline configure: (T) -> Unit
    ) {
        val task = project.tasks.register(name, T::class.java, *taskConstructorArgs)
        task.configure {
            configure(it)
        }
        task.dependsOnMinificationTask(
            project = project,
            variant = variant,
            shadowR8Task = shadowR8Task,
            debug = project.debug
        )
    }

    private fun RegularFileProperty.setSortedFileFrom(mergedFileProvider: RegularFileProperty) {
        set(mergedFileProvider.map { mergedFile ->
            val sortedFileName = "sorted-${mergedFile.asFile.name}"
            val sortedFile = mergedFile.asFile.resolveSibling(sortedFileName)
            RegularFile { sortedFile }
        })
    }

    public companion object {
        public fun computeCheckTaskName(variantName: String): String =
            "check${variantName.capitalize()}MergedProguard"

        public fun computeUpdateTaskName(variantName: String): String =
            "update${variantName.capitalize()}LockedProguard"
    }
}

private const val enabledProp = "avito.proguard-guard.enabled"
private const val debugProp = "avito.proguard-guard.debug"
