package com.avito.buildontarget

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun buildOnTargetTaskName(): String = "buildOnTargetCommit"

fun TaskContainer.buildOnTargetTask(): TaskProvider<BuildOnTargetCommitForTestTask> =
    typedNamed(buildOnTargetTaskName())

fun PluginManager.hasBuildOnTargetPlugin(): Boolean = hasPlugin("com.avito.android.build-on-target")
