package com.avito.android.signer

import com.avito.capitalize
import com.avito.kotlin.dsl.typedNamedOrNull
import org.gradle.api.file.Directory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer

internal const val PLUGIN_ID = "com.avito.android.sign-service"

internal const val DOCUMENTATION_URL = "https://avito-tech.github.io/avito-android/projects/internal/Signer/"

internal fun signApkTaskName(variantName: String) = "signApkViaService${variantName.capitalize()}"

internal fun signBundleTaskName(variantName: String) = "signBundleViaService${variantName.capitalize()}"

public fun TaskContainer.signedApkDir(variantName: String): Provider<Directory> {
    val taskName = signApkTaskName(variantName)

    return typedNamedOrNull<SignApkTask>(taskName)?.flatMap { it.signedArtifactDirectory }
        ?: Providers.notDefined()
}

public fun TaskContainer.signedBundleDir(variantName: String): Provider<Directory> {
    val taskName = signBundleTaskName(variantName)

    return typedNamedOrNull<SignBundleTask>(taskName)?.flatMap { it.signedArtifactDirectory }
        ?: Providers.notDefined()
}
