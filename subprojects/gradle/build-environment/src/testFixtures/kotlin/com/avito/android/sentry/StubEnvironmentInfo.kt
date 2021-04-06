package com.avito.android.sentry

import com.avito.utils.gradle.Environment

class StubEnvironmentInfo(
    override val node: String? = null,
    override val environment: Environment = Environment.CI,
    val teamcityBuildId: String? = null,
) : EnvironmentInfo {

    override fun teamcityBuildId(): String? = teamcityBuildId
}
