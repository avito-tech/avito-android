package com.avito.plugin

import com.avito.capitalize
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun legacyQappsUploadTaskName(variantName: String): String = "qappsUpload${variantName.capitalize()}"

internal fun qappsUploadUnsignedTaskName(variantName: String): String = "qappsUploadUnsigned${variantName.capitalize()}"

internal fun qappsUploadSignedTaskName(variantName: String): String = "qappsUploadSigned${variantName.capitalize()}"

public fun TaskContainer.legacyQappsTaskProvider(variantName: String): TaskProvider<QAppsUploadTask> =
    typedNamed(legacyQappsUploadTaskName(variantName))

public fun TaskContainer.qappsUploadUnsignedTaskProvider(variantName: String): TaskProvider<QAppsUploadTask> =
    typedNamed(qappsUploadUnsignedTaskName(variantName))

public fun TaskContainer.qappsUploadSignedTaskProvider(variantName: String): TaskProvider<QAppsUploadTask> =
    typedNamed(qappsUploadSignedTaskName(variantName))
