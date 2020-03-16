@file:Suppress("UnstableApiUsage")

package com.avito.utils.gradle

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalFloatProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import org.gradle.api.Project
import java.io.Serializable
import java.util.concurrent.TimeUnit

// Used in build.gradle's of avito
val Project.envArgs by ProjectProperty.lazy<EnvArgs>(scope = ROOT_PROJECT) { project ->
    if (project.buildEnvironment is BuildEnvironment.CI) EnvArgs.Impl(project) else EnvArgs.Stub
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
                FOR_STUDIO_RUN(-1),
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

    @Deprecated(replaceWith = ReplaceWith("build.url"), message = "since 2020.3.2")
    val buildUrl: String

    /**
     * номер билда, который составляется из версии авито_автоинкремент
     * например "56.0.393.40835"
     * todo это неправильно, должен быть только автоинкремент
     */
    @Deprecated(replaceWith = ReplaceWith("build.number"), message = "since 2020.3.2")
    val buildNumber: String

    /**
     * id билда в базе teamcity, используется для доступа по rest api
     * например: "7690353"
     */
    @Deprecated(replaceWith = ReplaceWith("build.id"), message = "since 2020.3.2")
    val buildId: String

    /**
     * id билд конфигурации
     * например: "AvitoAndroid_Build"
     */
    @Deprecated(replaceWith = ReplaceWith("build.type"), message = "since 2020.3.2")
    val buildTypeId: String

    val isRerunDisabled: Boolean

    val testDownsamplingFactor: Float?

    val build: Build

    class Impl(project: Project) : EnvArgs, Serializable {

        override val build: Build

        override val isRerunDisabled: Boolean =
            project.getBooleanProperty("avito.tests.disableRerunOnTargetBranch", default = false)

        override val testDownsamplingFactor: Float? =
            project.getOptionalFloatProperty("avito.tests.downsamplingFactor", default = null)


        init {
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
                    val id = if (buildEnvironment(project) is BuildEnvironment.CI) {
                        Build.Local.Id.FOR_LOCAL_KUBERNETES_RUN
                    } else {
                        Build.Local.Id.FOR_STUDIO_RUN
                    }
                    Build.Local(id)
                }
                else -> throw IllegalStateException("property avito.build must be 'teamcity' or 'local'")
            }
        }

        override val buildUrl: String = build.url
        override val buildTypeId: String = build.type
        override val buildNumber: String = build.number
        override val buildId: String = build.id.toString()
    }

    object Stub : EnvArgs, Serializable {
        override val buildUrl: String = ""
        override val buildTypeId: String = ""
        override val buildNumber: String = ""
        override val buildId: String = ""
        override val isRerunDisabled: Boolean = false
        override val testDownsamplingFactor: Float? = null
        override val build: Build = Build.Local(Build.Local.Id.FOR_STUDIO_RUN)
    }
}
