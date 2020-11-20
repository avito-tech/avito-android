package com.avito.android.sentry

import com.avito.git.Git
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project

internal class EnvironmentInfoImpl(private val project: Project, private val git: Git) : EnvironmentInfo {

    override val node: String? by lazy {
        when (environment) {
            is Environment.Local -> gitUserEmail ?: userName()
            is Environment.Mirakle -> userName()
            is Environment.CI -> teamcityAgentName()
            is Environment.Unknown -> "unknown"
        }
    }

    override val environment: Environment by lazy {
        val buildEnvironment = project.buildEnvironment
        when {
            (userName() == "teamcity") || (gitUserEmail == "teamcity") -> Environment.CI // we want ci metrics even if "-Pci=false" in CI
            buildEnvironment is BuildEnvironment.Local || buildEnvironment is BuildEnvironment.IDE -> Environment.Local
            buildEnvironment is BuildEnvironment.CI -> Environment.CI
            buildEnvironment is BuildEnvironment.Mirkale -> Environment.Mirakle
            else -> Environment.Unknown
        }
    }

    private val gitUserEmail: String? by lazy {
        if (hasGit) {
            git.config("user.email").toOption().orNull()?.substringBefore('@')
        } else {
            null
        }
    }

    private val hasGit: Boolean = (project.buildEnvironment !is BuildEnvironment.Mirkale)

    override val commit: String? by lazy {
        if (hasGit) {
            git.tryParseRev("HEAD").toOption().orNull()
        } else {
            null
        }
    }

    private fun userName(): String? = System.getProperty("user.name")

    private fun teamcityAgentName(): String? {
        return System.getenv("TEAMCITY_AGENT_NAME")
            ?: System.getProperty("TEAMCITY_AGENT_NAME", null)
    }

    // todo to one place
    override fun teamcityBuildId(): String? {
        return project.rootProject.getOptionalStringProperty("teamcityBuildId")
    }

    override fun isInvokedFromIde() = project.hasProperty("android.injected.invoked.from.ide")
}
