@file:Suppress("UnstableApiUsage")

package com.avito.utils.gradle

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import java.io.Serializable

val Project.envArgs by ProjectProperty.lazy<EnvArgs>(scope = ROOT_PROJECT) { project ->
    if (project.buildEnvironment is BuildEnvironment.CI) EnvArgs.Impl(project) else EnvArgs.Stub
}

interface EnvArgs {

    val buildUrl: String

    /**
     * номер билда, который составляется из версии авито_автоинкремент
     * например "56.0.393.40835"
     * todo это неправильно, должен быть только автоинкремент
     */
    val buildNumber: String

    /**
     * id билда в базе teamcity, используется для доступа по rest api
     * например: "7690353"
     */
    val buildId: String

    /**
     * id билд конфигурации
     * например: "AvitoAndroid_Build"
     */
    val buildTypeId: String

    val slackToken: String

    val isRerunDisabled: Boolean

    val testDownsamplingFactor: Float?

    class Impl(val project: Project) : EnvArgs, Serializable {

        private val teamcityUrl: String = project.getMandatoryStringProperty("teamcityUrl")

        override val buildTypeId: String = project.getMandatoryStringProperty("teamcityBuildType")

        override val buildNumber: String = project.getMandatoryStringProperty("buildNumber")

        override val buildId: String = project.getMandatoryStringProperty("teamcityBuildId")

        override val buildUrl: String = "$teamcityUrl/viewLog.html?buildId=$buildId&tab=buildLog"

        override val slackToken: String = project.getMandatoryStringProperty("slackToken")

        override val isRerunDisabled: Boolean =
            project.getBooleanProperty("avito.tests.disableRerunOnTargetBranch", default = false)

        override val testDownsamplingFactor: Float? =
            project.getFloatProperty("avito.tests.downsamplingFactor", default = null)
    }

    object Stub : EnvArgs, Serializable {
        override val buildUrl: String = ""
        override val buildTypeId: String = ""
        override val buildNumber: String = ""
        override val buildId: String = ""
        override val slackToken: String = ""
        override val isRerunDisabled: Boolean = false
        override val testDownsamplingFactor: Float? = null
    }
}
