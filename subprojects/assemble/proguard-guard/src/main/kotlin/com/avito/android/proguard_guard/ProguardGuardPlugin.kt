package com.avito.android.proguard_guard

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.avito.android.isAndroidApp
import com.avito.android.proguard_guard.task.CheckMergedConfigurationTask
import com.avito.android.proguard_guard.task.UpdateLockedConfigurationTask
import com.avito.capitalize
import com.avito.kotlin.dsl.namedOrNull
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create
import java.lang.IllegalStateException

public class ProguardGuardPlugin : Plugin<Project> {

    private val Project.pluginIsEnabled: Boolean
        get() = providers
            .gradleProperty(enabledProp)
            .map { it.toBoolean() }
            .getOrElse(true)

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
        project.tasks.register(
            "check${variant.capitalizedName()}MergedProguard",
            CheckMergedConfigurationTask::class.java,
        ) { task ->
            task.group = "Proguard guard"
            task.description = "Compare ${variant.capitalizedName()} proguard config with locked config"
            task.mergedConfigurationFile.set(extension.mergedConfigurationFile)
            task.lockedConfigurationFile.set(extension.lockedConfigurationFile)
            task.failOnDifference.set(extension.failOnDifference)
            task.diffFile.set(extension.outputFile)
            task.dependsOnMinification(project, variant)
        }
    }

    private fun registerUpdateTask(
        project: Project,
        variant: ApplicationVariant,
        extension: BuildVariantProguardGuardConfiguration,
    ) {
        project.tasks.register(
            "update${variant.capitalizedName()}LockedProguard",
            UpdateLockedConfigurationTask::class.java,
        ) { task ->
            task.group = "Proguard guard"
            task.description = "Update locked proguard config with ${variant.capitalizedName()}"
            task.mergedConfigurationFile.set(extension.mergedConfigurationFile)
            task.lockedConfigurationFile.set(extension.lockedConfigurationFile)
            task.dependsOnMinification(project, variant)
        }
    }

    private fun Task.dependsOnMinification(
        project: Project,
        variant: ApplicationVariant,
    ) {
        val minificationTaskName = "minify${variant.capitalizedName()}WithR8"
        project.tasks.namedOrNull(minificationTaskName)
            ?: throw IllegalStateException(
                "Task $minificationTaskName was not found in project ${project.path}. " +
                    "You probably forgot to set minifyEnabled to true."
            )
        dependsOn(minificationTaskName)
    }

    private fun Variant.capitalizedName(): String = name.capitalize()
}

private const val enabledProp = "avito.proguard-guard.enabled"
