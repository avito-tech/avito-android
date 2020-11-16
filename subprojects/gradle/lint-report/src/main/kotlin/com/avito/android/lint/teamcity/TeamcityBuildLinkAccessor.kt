package com.avito.android.lint.teamcity

import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.utils.gradle.envArgs
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.Project

internal interface TeamcityBuildLinkAccessor {

    fun getBuildUrl(): HttpUrl

    fun getLintArtifactUrl(): HttpUrl

    class Impl(private val project: Project) : TeamcityBuildLinkAccessor {

        private val buildId = project.envArgs.build.id

        private val teamcityUrl = project.getMandatoryStringProperty("teamcityUrl").removeSuffix("/")

        override fun getBuildUrl(): HttpUrl {
            return "$teamcityUrl/viewLog.html?buildId=$buildId".toHttpUrl()
        }

        override fun getLintArtifactUrl(): HttpUrl {
            return "$teamcityUrl/repository/download/AvitoAndroid_Build/$buildId:id/${project.name}/build/reports/lint-results-release.html".toHttpUrl()
        }
    }
}
