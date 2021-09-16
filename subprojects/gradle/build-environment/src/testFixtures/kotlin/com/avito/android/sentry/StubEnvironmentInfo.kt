package com.avito.android.sentry

import com.avito.utils.gradle.Environment

public class StubEnvironmentInfo(
    override val node: String? = null,
    override val environment: Environment = Environment.CI,
    public val teamcityBuildId: String? = null,
) : EnvironmentInfo {

    override fun teamcityBuildId(): String? = teamcityBuildId
}
