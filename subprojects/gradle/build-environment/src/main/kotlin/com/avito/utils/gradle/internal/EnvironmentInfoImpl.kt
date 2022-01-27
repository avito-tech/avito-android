package com.avito.utils.gradle.internal

import com.avito.android.Result
import com.avito.android.sentry.EnvironmentInfo
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.Environment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project

internal class EnvironmentInfoImpl(
    private val project: Project,
    private val gitUserEmailProvider: Lazy<Result<String>>
) : EnvironmentInfo {

    private val gitUserEmail: String? by lazy {
        if (hasGit) {
            gitUserEmailProvider.value.fold(
                { email -> email.substringBefore('@') },
                { _ -> null }
            )
        } else {
            null
        }
    }

    override val node: String? by lazy {
        when (environment) {
            is Environment.Local -> gitUserEmail ?: userName()
            is Environment.CI -> teamcityAgentName()
            is Environment.Unknown -> "unknown"
        }
    }

    override val environment: Environment by lazy {
        val buildEnvironment = project.buildEnvironment
        when {
            // we want ci metrics even if "-Pci=false" in CI
            userName() == "teamcity" || gitUserEmail == "teamcity" -> Environment.CI
            buildEnvironment is BuildEnvironment.Local || buildEnvironment is BuildEnvironment.IDE -> Environment.Local
            buildEnvironment is BuildEnvironment.CI -> Environment.CI
            else -> Environment.Unknown
        }
    }

    private val hasGit: Boolean = project.buildEnvironment !is BuildEnvironment.Mirkale

    private fun userName(): String? = System.getProperty("user.name")

    private fun teamcityAgentName(): String? {
        return System.getenv("TEAMCITY_AGENT_NAME")
            ?: System.getProperty("TEAMCITY_AGENT_NAME", null)
    }

    // todo to one place
    override fun teamcityBuildId(): String? {
        return project.rootProject.getOptionalStringProperty("teamcityBuildId")
    }
}
