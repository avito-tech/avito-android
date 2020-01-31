package com.avito.android.docker

import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import java.io.Serializable

interface DockerCredentials : Serializable {
    val login: String
    val password: String

    class Implementation(project: Project) : DockerCredentials,
        Serializable {

        override val login: String by lazy {
            project.getOptionalStringProperty("dockerLogin", default = "")
        }

        override val password: String by lazy {
            project.getOptionalStringProperty("dockerPassword", default = "")
        }
    }
}
