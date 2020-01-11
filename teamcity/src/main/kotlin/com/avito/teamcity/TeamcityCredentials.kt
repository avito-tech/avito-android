package com.avito.teamcity

import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import java.io.Serializable

interface TeamcityCredentials : Serializable {

    val url: String
    val user: String
    val password: String

    class Impl(project: Project) : TeamcityCredentials,
        Serializable {
        override val url: String by lazy { project.getMandatoryStringProperty("teamcityUrl") }
        override val user: String by lazy { project.getMandatoryStringProperty("teamcityApiUser") }
        override val password: String by lazy { project.getMandatoryStringProperty("teamcityApiPassword") }
    }
}

val Project.teamcityCredentials: TeamcityCredentials
    get() = TeamcityCredentials.Impl(this)
