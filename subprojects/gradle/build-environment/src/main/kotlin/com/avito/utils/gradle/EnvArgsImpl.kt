package com.avito.utils.gradle

import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import java.io.Serializable

internal class EnvArgsImpl(project: Project) : EnvArgs, Serializable {

    /**
     * todo make local by default?
     */
    override val build: EnvArgs.Build = when (project.getOptionalStringProperty("avito.build", "teamcity")) {
        "teamcity" -> {
            val teamcityBuildId = project.getMandatoryIntProperty("teamcityBuildId")
            EnvArgs.Build.Teamcity(
                id = teamcityBuildId,
                url = project.getMandatoryStringProperty("teamcityUrl") +
                        "/viewLog.html?buildId=$teamcityBuildId&tab=buildLog",
                number = project.getMandatoryStringProperty("buildNumber"),
                type = "teamcity-${project.getMandatoryStringProperty("teamcityBuildType")}"
            )
        }
        "local" ->
            EnvArgs.Build.Local(
                id = EnvArgs.Build.generatedId,
                type = "local-${System.getProperty("user.name")}",
            )

        "sandbox" ->
            EnvArgs.Build.Sandbox(
                id = EnvArgs.Build.generatedId,
                type = "sandbox-${project.getMandatoryStringProperty("sandboxBuildType", allowBlank = false)}",
            )

        else ->
            throw IllegalStateException("property avito.build must be 'teamcity', 'local' or 'sandbox'")
    }
}
