package com.avito.teamcity

import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import org.gradle.api.provider.Provider

public val Project.teamcityCredentials: Provider<TeamcityCredentials>
    get() = provider {
        TeamcityCredentials(
            project.getMandatoryStringProperty("teamcityUrl"),
            project.getMandatoryStringProperty("teamcityApiUser"),
            project.getMandatoryStringProperty("teamcityApiPassword")
        )
    }
