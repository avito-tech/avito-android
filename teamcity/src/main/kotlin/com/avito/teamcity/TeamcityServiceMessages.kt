package com.avito.teamcity

import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.logging.Logger

val Project.teamcityServiceMessages: TeamcityServiceMessages
    get() = if (buildEnvironment is BuildEnvironment.CI) TeamcityServiceMessages.Impl(logger) else TeamcityServiceMessages.Stub

/**
 * Teamcity build parameters read and interaction via service messages.
 * Do not use it for metrics. There is a better option - statsd.
 *
 * Be careful with debugging in TeamCity:
 * - Service messages are hidden in TeamCity logs
 * - Values from personal builds are not used
 *
 * @see serviceMessages https://confluence.jetbrains.com/display/TCD9/Build+Script+Interaction+with+TeamCity#BuildScriptInteractionwithTeamCity-ReportingMessagesForBuildLog).
 */
interface TeamcityServiceMessages {

    fun setParameter(key: String, value: String)

    class Impl(private val logger: Logger) : TeamcityServiceMessages {
        override fun setParameter(key: String, value: String) {
            logger.lifecycle("##teamcity[setParameter name='$key' value='$value']")
        }
    }

    object Stub : TeamcityServiceMessages {
        override fun setParameter(key: String, value: String) {
        }
    }
}
