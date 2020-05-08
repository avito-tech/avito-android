@file:Suppress("UnstableApiUsage")

package com.avito.utils.gradle

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import java.io.Serializable
import java.util.concurrent.TimeUnit

// Used in build.gradle's of avito
val Project.envArgs by ProjectProperty.lazy<EnvArgs>(scope = ROOT_PROJECT) { project ->
    EnvArgs.Impl(project)
}

interface EnvArgs {

    sealed class Build : Serializable {
        /**
         * @todo change to string after refactor [TestRunEnvironment.teamcityBuildId]
         */
        abstract val id: Int
        abstract val url: String
        abstract val number: String
        abstract val type: String

        internal class Local(id: Id) : Build() {
            override val id = id.id
            override val url = "No url. This is local build"
            override val number = "local"
            override val type = "local-${userName}"

            internal enum class Id(val id: Int) {
                FOR_LOCAL_KUBERNETES_RUN(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt())
            }

            companion object {
                private val userName = System.getProperty("user.name")
            }
        }

        data class Teamcity(
            override val id: Int,
            override val url: String,
            override val number: String,
            override val type: String
        ) : Build()

    }

    val isRerunDisabled: Boolean

    val build: Build

    class Impl(project: Project) : EnvArgs, Serializable {

        override val build: Build

        override val isRerunDisabled: Boolean =
            project.getBooleanProperty("avito.tests.disableRerunOnTargetBranch", default = false)

        init {
            // todo make local by default?
            build = when (project.getOptionalStringProperty("avito.build", "teamcity")) {
                "teamcity" -> {
                    val teamcityBuildId = project.getMandatoryIntProperty("teamcityBuildId")
                    Build.Teamcity(
                        id = teamcityBuildId,
                        url = "${project.getMandatoryStringProperty("teamcityUrl")}/viewLog.html?buildId=$teamcityBuildId&tab=buildLog",
                        number = project.getMandatoryStringProperty("buildNumber"),
                        type = "teamcity-${project.getMandatoryStringProperty("teamcityBuildType")}"
                    )
                }
                "local" -> {
                    Build.Local(Build.Local.Id.FOR_LOCAL_KUBERNETES_RUN)
                }
                else -> throw IllegalStateException("property avito.build must be 'teamcity' or 'local'")
            }
        }

    }
}
