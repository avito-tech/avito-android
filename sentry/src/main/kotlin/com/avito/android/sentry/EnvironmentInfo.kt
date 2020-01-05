package com.avito.android.sentry

import com.avito.git.Git
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import java.net.InetAddress

/**
 * Use [Project.environmentInfo] to gain instance
 */
interface EnvironmentInfo {
    val node: String?
    val environment: Environment
    val commit: String?
    fun teamcityBuildId(): String?
    fun isInvokedFromIde(): Boolean
}

internal class EnvironmentInfoImpl(private val project: Project, private val git: Git) : EnvironmentInfo {

    override val node: String? by lazy {
        when (environment) {
            is Environment.Local -> gitUserEmail ?: userName() ?: hostname()
            is Environment.Mainframer -> userName()
            is Environment.CI -> teamcityAgentName()
            is Environment.Unknown -> hostname()
        }
    }

    override val environment: Environment by lazy {
        val buildEnvironment = project.buildEnvironment
        when {
            (userName() == "teamcity") || (gitUserEmail == "teamcity") -> Environment.CI // we want ci metrics even if "-Pci=false" in CI
            buildEnvironment is BuildEnvironment.Local || buildEnvironment is BuildEnvironment.IDE -> Environment.Local
            buildEnvironment is BuildEnvironment.CI -> Environment.CI
            buildEnvironment is BuildEnvironment.Mainframer -> Environment.Mainframer
            else -> Environment.Unknown
        }
    }

    private fun userName(): String? = System.getProperty("user.name")

    private fun teamcityAgentName(): String? {
        return System.getenv("TEAMCITY_AGENT_NAME")
            ?: System.getProperty("TEAMCITY_AGENT_NAME", null)
    }

    override fun teamcityBuildId(): String? {
        return project.rootProject.getOptionalStringProperty("teamcityBuildId")
    }

    private val gitUserEmail: String? by lazy {
        if (hasGit) {
            git.config("user.email").toOption().orNull()
                ?.substringBefore('@')
        } else {
            null
        }
    }

    private val hasGit: Boolean = (project.buildEnvironment !is BuildEnvironment.Mainframer)

    override val commit: String? by lazy {
        if (hasGit) {
            git.tryParseRev("HEAD").toOption().orNull()
        } else {
            null
        }
    }

    override fun isInvokedFromIde() = project.hasProperty("android.injected.invoked.from.ide")

    private fun hostname(): String {
        return InetAddress.getLocalHost().hostName
            .substringBefore(".local")
            .substringBefore(".msk.avito.ru")
    }

}

/**
 * @param publicName это название используется в graphite событиях
 */
sealed class Environment(val publicName: String) {
    object Local : Environment("local")
    object Mainframer : Environment("mainframer")
    object CI : Environment("ci")
    object Unknown : Environment("_")
}
