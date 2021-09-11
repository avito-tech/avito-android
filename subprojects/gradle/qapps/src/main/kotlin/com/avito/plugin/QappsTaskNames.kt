package com.avito.plugin

import com.avito.capitalize
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun qappsTaskName(variantName: String): String = "qappsUpload${variantName.capitalize()}"

public fun TaskContainer.qappsTaskProvider(variantName: String): TaskProvider<QAppsUploadTask> =
    typedNamed(qappsTaskName(variantName))
