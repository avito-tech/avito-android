@file:Suppress("UnstableApiUsage")

package com.avito.plugin

import com.android.build.api.component.ComponentIdentity
import com.avito.android.taskName
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun signApkTaskName(variantName: String): String = "signApkViaService${variantName.capitalize()}"

internal fun signApkTaskName(component: ComponentIdentity): String = taskName("signApkViaService", component)

internal fun signBundleTaskName(variantName: String): String = "signBundleViaService${variantName.capitalize()}"

internal fun signBundleTaskName(component: ComponentIdentity): String = taskName("signBundleViaService", component)

fun TaskContainer.signedApkTaskProvider(variantName: String): TaskProvider<SignTask> {
    return typedNamed(signApkTaskName(variantName))
}

fun TaskContainer.signedApkTaskProvider(component: ComponentIdentity): TaskProvider<SignTask> {
    return typedNamed(signApkTaskName(component))
}

fun TaskContainer.signedBundleTaskProvider(variantName: String): TaskProvider<SignTask> {
    return typedNamed(signBundleTaskName(variantName))
}

fun TaskContainer.signedBundleTaskProvider(component: ComponentIdentity): TaskProvider<SignTask> {
    return typedNamed(signBundleTaskName(component))
}
