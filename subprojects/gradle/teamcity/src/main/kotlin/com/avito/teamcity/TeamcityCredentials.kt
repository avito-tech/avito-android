package com.avito.teamcity

import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import java.io.Serializable

interface TeamcityCredentials : Serializable {

    val url: String
    val user: String
    val password: String

    class Impl(
        override val url: String,
        override val user: String,
        override val password: String
    ) : TeamcityCredentials, Serializable {

        constructor(project: Project) : this(
            project.getMandatoryStringProperty("teamcityUrl"),
            project.getMandatoryStringProperty("teamcityApiUser"),
            project.getMandatoryStringProperty("teamcityApiPassword")
        )
    }
}

val Project.teamcityCredentials: TeamcityCredentials
    get() = TeamcityCredentials.Impl(this)
