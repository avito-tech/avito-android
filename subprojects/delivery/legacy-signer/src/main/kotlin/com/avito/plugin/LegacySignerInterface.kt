package com.avito.plugin

import com.android.build.api.variant.ComponentIdentity
import com.avito.android.taskName
import com.avito.capitalize
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun legacySignApkTaskName(variantName: String): String =
    "legacySignApkViaService${variantName.capitalize()}"

internal fun legacySignApkTaskName(component: ComponentIdentity): String =
    taskName("legacySignApkViaService", component)

internal fun legacySignBundleTaskName(variantName: String): String =
    "legacySignBundleViaService${variantName.capitalize()}"

internal fun legacySignBundleTaskName(component: ComponentIdentity): String =
    taskName("legacySignBundleViaService", component)

public fun TaskContainer.legacySignedApkTaskProvider(
    variantName: String
): TaskProvider<LegacySignApkTask> {
    return typedNamed(legacySignApkTaskName(variantName))
}

public fun TaskContainer.legacySignedApkTaskProvider(
    component: ComponentIdentity
): TaskProvider<LegacySignApkTask> {
    return typedNamed(legacySignApkTaskName(component))
}

public fun TaskContainer.legacySignedBundleTaskProvider(
    variantName: String
): TaskProvider<LegacySignBundleTask> {
    return typedNamed(legacySignBundleTaskName(variantName))
}

public fun TaskContainer.legacySignedBundleTaskProvider(
    component: ComponentIdentity
): TaskProvider<LegacySignBundleTask> {
    return typedNamed(legacySignBundleTaskName(component))
}
