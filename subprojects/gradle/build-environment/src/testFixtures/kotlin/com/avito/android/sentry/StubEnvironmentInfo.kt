package com.avito.android.sentry

import com.avito.utils.gradle.Environment

class StubEnvironmentInfo(
    override val node: String? = null,
    override val environment: Environment = Environment.CI,
) : EnvironmentInfo
