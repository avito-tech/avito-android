package com.avito.android.lint.teamcity

import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.utils.gradle.envArgs
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.Project

interface TeamcityBuildLinkAccessor {

    fun getLink(): HttpUrl

    class Impl(private val project: Project) : TeamcityBuildLinkAccessor {

        override fun getLink(): HttpUrl {
            val buildId = project.envArgs.build.id
            val teamcityUrl = project.getMandatoryStringProperty("teamcityUrl").removeSuffix("/")
            return "${teamcityUrl}/repository/download/AvitoAndroid_Build/${buildId}:id/${project.name}/build/reports/lint-results-release.html".toHttpUrl()
        }
    }
}
