package com.avito.utils.gradle.internal

import com.avito.android.sentry.EnvironmentInfo
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.Environment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project

internal class EnvironmentInfoImpl(
    private val project: Project,
) : EnvironmentInfo {

    override val node: String? by lazy {
        when (environment) {
            is Environment.Local -> userName()
            is Environment.CI -> "_"
            is Environment.Unknown -> "unknown"
        }
    }

    override val environment: Environment by lazy {
        val buildEnvironment = project.buildEnvironment
        when {
            // we want ci metrics even if "-Pci=false" in CI
            userName() == "teamcity" -> Environment.CI
            buildEnvironment is BuildEnvironment.Local || buildEnvironment is BuildEnvironment.IDE -> Environment.Local
            buildEnvironment is BuildEnvironment.CI -> Environment.CI
            else -> Environment.Unknown
        }
    }

    private fun userName(): String? = System.getProperty("user.name")
}
