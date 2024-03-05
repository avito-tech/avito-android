package com.avito.android.proguard_guard.shadowr8

import com.android.build.api.component.analytics.AnalyticsEnabledApplicationVariant
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.impl.ApplicationVariantImpl
import com.android.build.gradle.internal.component.ConsumableCreationConfig
import com.android.build.gradle.internal.tasks.R8Task
import com.avito.android.capitalizedName
import com.avito.android.proguard_guard.task.ProguardGuardTask
import com.avito.android.proguard_guard.task.setMergedFileIfNotPresent
import com.avito.kotlin.dsl.typedNamed
import com.avito.kotlin.dsl.typedNamedOrNull
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal fun TaskProvider<out ProguardGuardTask>.dependsOnMinificationTask(
    project: Project,
    variant: ApplicationVariant,
    shadowR8Task: Boolean,
    debug: Boolean,
) {
    val creationConfig: ConsumableCreationConfig = variant.toConsumableCreationConfig()
//    check(creationConfig.minifiedEnabled) {
//        "Minification is disabled for variant ${variant.name}. " +
//            "You probably forgot to set minifyEnabled to true."
//    }

    if (shadowR8Task) {
        val shadowTaskProvider = getShadowR8TaskProvider(project, creationConfig, debug)
        configure { proguardGuardTask ->
            proguardGuardTask.dependsOn(shadowTaskProvider)
            proguardGuardTask.setMergedFileIfNotPresent(shadowTaskProvider)
        }
    } else {
        configure { proguardGuardTask ->
            val originalR8TaskProvider = getOriginalR8TaskProvider(project, variant, debug)
            proguardGuardTask.dependsOn(originalR8TaskProvider)
            proguardGuardTask.setMergedFileIfNotPresent(originalR8TaskProvider)
        }
    }
}

private fun getShadowR8TaskProvider(
    project: Project,
    creationConfig: ConsumableCreationConfig,
    debug: Boolean,
): TaskProvider<R8Task> {
    val shadowTaskCreator = ShadowR8TaskCreator(creationConfig)
    val shadowTaskName = shadowTaskCreator.name

    val existentShadowTask = project.tasks.typedNamedOrNull<R8Task>(shadowTaskName)
    val shadowTaskProvider = existentShadowTask ?: shadowTaskCreator.registerIn(project)
    if (debug) {
        printR8Task(shadowTaskProvider)
    }
    return shadowTaskProvider
}

private fun getOriginalR8TaskProvider(
    project: Project,
    variant: ApplicationVariant,
    debug: Boolean,
): TaskProvider<R8Task> {
    val minificationTaskName = "minify${variant.capitalizedName()}WithR8"
    val minificationTaskProvider = project.tasks.typedNamed<R8Task>(minificationTaskName)
    if (debug) {
        printR8Task(minificationTaskProvider)
    }
    return minificationTaskProvider
}

private fun printR8Task(taskProvider: TaskProvider<R8Task>) {
    taskProvider.configure { r8Task ->
        r8Task.doLast {
            r8Task.print()
        }
    }
}

private fun ApplicationVariant.toConsumableCreationConfig(): ConsumableCreationConfig {
    return when (this) {
        is ApplicationVariantImpl -> this
        is AnalyticsEnabledApplicationVariant -> delegate as ConsumableCreationConfig
        else -> throw IllegalStateException(
            "Unable to retrieve instance of ConsumableCreationConfig. Please contact plugin developer."
        )
    }
}
