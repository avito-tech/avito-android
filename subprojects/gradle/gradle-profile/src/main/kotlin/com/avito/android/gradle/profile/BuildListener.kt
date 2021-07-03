package com.avito.android.gradle.profile

import org.gradle.BuildResult

internal interface BuildListener : TaskExecutionListener {

    fun buildFinished(result: BuildResult, profile: BuildProfile)
}
