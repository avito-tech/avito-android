package com.avito.android.build_verdict.internal

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

internal abstract class BaseBuildListener : BuildListener {

    override fun settingsEvaluated(settings: Settings) {}

    override fun projectsLoaded(gradle: Gradle) {}

    override fun projectsEvaluated(gradle: Gradle) {}

    @Deprecated("Incompatible with configuration cache")
    override fun buildFinished(result: BuildResult) {}
}
