@file:Suppress("DEPRECATION")

package com.avito.android.tech_budget.internal.detekt

import com.android.build.gradle.api.BaseVariant
import com.avito.android.androidBaseExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.detekt.tasks.AvitoDetektReportTask
import com.avito.android.tech_budget.internal.detekt.tasks.TechBudgetDetektTask
import com.avito.android.tech_budget.techBudgetExtension
import com.avito.capitalize
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets

public class DetektConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (project.isRoot() || !project.techBudgetExtension.detekt.enabled.get()) {
            return
        }

        val variantsFilter = AndroidVariantsFilter(
            targetVariantName = "debug",
            fallbacks = setOf("release")
        )

        val reportTask = project.tasks.register<AvitoDetektReportTask>("detektReleaseReport")

        project.plugins.withType<KotlinBasePlugin> {
            project.kotlinExtension.targets.forEach { kotlinTarget ->
                if (kotlinTarget is KotlinAndroidTarget) {
                    kotlinTarget.compilations
                        .matching { it.androidVariant.baseVariant == it.androidVariant }
                        .matching { variantsFilter.isVariantSuitable(it.androidVariant) }
                        .all { compilation ->
                            val variantTask = project.registerAndroidDetektTasks(
                                compilation.androidVariant,
                                compilation.defaultSourceSet.kotlin.sourceDirectories,
                            )
                            reportTask.configure { it.reports.from(variantTask.flatMap { it.warnings }) }

                            val unitTestVariant = compilation.androidVariant.unitTestVariant
                            if (unitTestVariant != null) {
                                val unitTestTask = project.registerAndroidDetektTasks(unitTestVariant)
                                reportTask.configure { it.reports.from(unitTestTask.flatMap { it.warnings }) }
                            }
                        }
                } else {
                    kotlinTarget.compilations.all {
                        val task = project.registerJvmDetektTask(it)
                        reportTask.configure {
                            it.reports.from(task.flatMap { it.warnings })
                        }
                    }
                }
            }
        }
    }

    private fun Project.registerJvmDetektTask(compilation: KotlinCompilation<*>): TaskProvider<TechBudgetDetektTask> {
        return tasks.register<TechBudgetDetektTask>("detektTechBudget${compilation.name.capitalize()}") {
            setupWithDefaults {
                description = "Runs detekt with type resolution inside the module."
                config.setFrom(techBudgetExtension.detekt.configFiles)
                enableKotlinTypeResolution(compilation)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun Project.registerAndroidDetektTasks(
        variant: BaseVariant,
        additionalVariantSources: FileCollection? = null,
    ): TaskProvider<TechBudgetDetektTask> {
        val bootClasspath = project.files(project.androidBaseExtension.bootClasspath)
        return tasks.register<TechBudgetDetektTask>("detektTechBudget${variant.name.capitalize()}") {
            setupWithDefaults {
                description = "Runs detekt with type resolution inside the module."
                config.setFrom(techBudgetExtension.detekt.configFiles)
                enableAndroidTypeResolution(
                    variant = variant,
                    additionalVariantSources = additionalVariantSources,
                    bootClasspath = bootClasspath,
                )
            }
        }
    }
}

private class AndroidVariantsFilter(
    val targetVariantName: String,
    val fallbacks: Set<String>
)

@Suppress("DEPRECATION")
private fun AndroidVariantsFilter.isVariantSuitable(variant: BaseVariant): Boolean {
    val baseVariantName = variant.baseVariant.name
    val variantName = variant.name
    return baseVariantName == targetVariantName || variantName == targetVariantName || variantName in fallbacks
}
