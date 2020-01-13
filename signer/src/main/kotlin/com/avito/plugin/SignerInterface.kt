@file:Suppress("UnstableApiUsage")

package com.avito.plugin

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun signApkTaskName(variantName: String): String = "signApkViaService${variantName.capitalize()}"

internal fun signBundleTaskName(variantName: String): String = "signBundleViaService${variantName.capitalize()}"

fun TaskContainer.signedApkTaskProvider(variantName: String): TaskProvider<SignTask> {
    return typedNamed(signApkTaskName(variantName))
}

fun TaskContainer.signedBundleTaskProvider(variantName: String): TaskProvider<SignTask> {
    return typedNamed(signBundleTaskName(variantName))
}
