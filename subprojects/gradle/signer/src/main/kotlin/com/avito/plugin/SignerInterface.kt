@file:Suppress("UnstableApiUsage")

package com.avito.plugin

import com.android.build.api.component.ComponentIdentity
import com.avito.android.taskName
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import java.util.Locale

internal fun signApkTaskName(variantName: String): String =
    "signApkViaService${variantName.capitalize(Locale.getDefault())}"

internal fun signApkTaskName(component: ComponentIdentity): String = taskName("signApkViaService", component)

internal fun signBundleTaskName(variantName: String): String =
    "signBundleViaService${variantName.capitalize(Locale.getDefault())}"

internal fun signBundleTaskName(component: ComponentIdentity): String = taskName("signBundleViaService", component)

fun TaskContainer.signedApkTaskProvider(variantName: String): TaskProvider<SignApkTask> {
    return typedNamed(signApkTaskName(variantName))
}

fun TaskContainer.signedApkTaskProvider(component: ComponentIdentity): TaskProvider<SignApkTask> {
    return typedNamed(signApkTaskName(component))
}

fun TaskContainer.signedBundleTaskProvider(variantName: String): TaskProvider<SignBundleTask> {
    return typedNamed(signBundleTaskName(variantName))
}

fun TaskContainer.signedBundleTaskProvider(component: ComponentIdentity): TaskProvider<SignBundleTask> {
    return typedNamed(signBundleTaskName(component))
}
