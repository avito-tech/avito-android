package com.avito.plugin

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.capitalize
import com.avito.kotlin.dsl.typedNamedOrNull
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer

internal fun signApkTaskName(variantName: String) = "signApkViaService${variantName.capitalize()}"

internal fun signBundleTaskName(variantName: String) = "signBundleViaService${variantName.capitalize()}"

public fun TaskContainer.signedApk(variantName: String): Provider<RegularFile> {
    val taskName = signApkTaskName(variantName)
    val task = typedNamedOrNull<SignApkTask>(taskName)

    return if (task != null) {
        task.flatMap { it.signedArtifact }
    } else {
        throw buildConfigurationError(taskName, variantName)
    }
}

public fun TaskContainer.signedBundle(variantName: String): Provider<RegularFile> {
    val taskName = signBundleTaskName(variantName)
    val task = typedNamedOrNull<SignBundleTask>(taskName)

    return if (task != null) {
        task.flatMap { it.signedArtifact }
    } else {
        throw buildConfigurationError(taskName, variantName)
    }
}

private fun buildConfigurationError(taskName: String, variantName: String): Throwable {
    return Problem(
        shortDescription = "Task $taskName not found",
        context = "Trying to access signed artifact for $variantName",
        because = "Token not set for this build variant",
        documentedAt = "https://avito-tech.github.io/avito-android/projects/internal/Signer/"
    ).asRuntimeException()
}
