package com.avito.plugin

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer

internal fun qappsTaskName(variantName: String): String = "qappsUpload${variantName.capitalize()}"

fun TaskContainer.qappsTaskProvider(variantName: String) = typedNamed<QAppsUploadTask>(qappsTaskName(variantName))
