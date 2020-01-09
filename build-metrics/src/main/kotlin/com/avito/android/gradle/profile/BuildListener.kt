package com.avito.android.gradle.profile

import org.gradle.BuildResult

interface BuildListener : TaskExecutionListener {
    fun buildFinished(result: BuildResult, profile: BuildProfile)
}
