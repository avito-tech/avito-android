package com.avito.android.plugin

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal const val docsDeployTaskName: String = "deployDocs"

internal const val docsCheckTaskName: String = "checkDocs"

fun TaskContainer.docsDeployTask(): TaskProvider<DeployDocsTask> = typedNamed(docsDeployTaskName)

fun TaskContainer.docsCheckTask(): TaskProvider<DocsCheckTask> = typedNamed(docsCheckTaskName)
