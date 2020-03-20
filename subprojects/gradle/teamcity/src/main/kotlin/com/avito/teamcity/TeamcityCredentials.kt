package com.avito.teamcity

import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project

val Project.teamcityCredentials: TeamcityCredentials
    get() = TeamcityCredentials(
        project.getMandatoryStringProperty("teamcityUrl"),
        project.getMandatoryStringProperty("teamcityApiUser"),
        project.getMandatoryStringProperty("teamcityApiPassword")
    )
