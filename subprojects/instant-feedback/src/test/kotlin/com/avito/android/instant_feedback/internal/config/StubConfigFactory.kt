package com.avito.android.instant_feedback.internal.config

import com.avito.android.Result

internal object StubConfigFactory : ConfigFactory {
    override fun create(): Result<Config> = Result.Success(Config(serviceEndpoint = "http://localhost:8080"))
}
