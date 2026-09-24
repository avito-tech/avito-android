@file:Suppress("DEPRECATION")

package com.avito.android.tech_budget.internal.detekt

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Component
import com.android.build.api.variant.HasUnitTest
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
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
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

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
                    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
                    androidComponents.onVariants { variant ->
                        if (!variantsFilter.isVariantSuitable(variant)) return@onVariants

                        val bootClasspath = project.files(androidComponents.sdkComponents.bootClasspath)
                        val variantTask = project.registerAndroidDetektTasks(variant, bootClasspath)
                        reportTask.configure { it.reports.from(variantTask.flatMap { it.warnings }) }

                        val unitTest = (variant as? HasUnitTest)?.unitTest
                        if (unitTest != null) {
                            val unitTestTask = project.registerAndroidDetektTasks(unitTest, bootClasspath)
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

    private fun Project.registerAndroidDetektTasks(
        component: Component,
        bootClasspath: FileCollection,
    ): TaskProvider<TechBudgetDetektTask> {
        val task = tasks.register<TechBudgetDetektTask>("detektTechBudget${component.name.capitalize()}") {
            setupWithDefaults {
                description = "Runs detekt with type resolution inside the module."
                config.setFrom(techBudgetExtension.detekt.configFiles)
                enableAndroidTypeResolution(
                    component = component,
                    bootClasspath = bootClasspath,
                )
            }
        }
        component.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
            .use(task)
            .toGet(
                ScopedArtifact.CLASSES,
                TechBudgetDetektTask::projectClassesJars,
                TechBudgetDetektTask::projectClassesDirectories,
            )
        return task
    }
}

private class AndroidVariantsFilter(
    val targetVariantName: String,
    val fallbacks: Set<String>
)

private fun AndroidVariantsFilter.isVariantSuitable(variant: Variant): Boolean {
    return variant.name == targetVariantName || variant.name in fallbacks
}

/**
 *  This is the copy of KGP implementation
 *  KotlinProjectExtension.targets is internal in Kotlin Gradle Plugin since Kotlin 2.0
 *  See KT-61463 - kotlin commit 282d2f4
 *  Recommended by Kotlin build tools teamlead - https://tinyurl.com/bddrd5pa
 */
private val KotlinProjectExtension.targets: Iterable<KotlinTarget>
    get() = when (this) {
        is KotlinSingleTargetExtension<*> -> listOf(this.target)
        is KotlinMultiplatformExtension -> targets
        else -> error("Unexpected 'kotlin' extension $this")
    }
