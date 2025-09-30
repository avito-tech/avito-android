package com.avito.android.instant_feedback.internal.config

import com.avito.android.Result

internal object EnvBasedConfigFactory : ConfigFactory {
    override fun create(): Result<Config> {
        val serviceEndpoint = System.getenv("SERVICE_ENDPOINT")
        return if (serviceEndpoint != null) {
            Result.Success(Config(serviceEndpoint = serviceEndpoint))
        } else {
            Result.Failure(RuntimeException("Missing SERVICE_ENDPOINT environment variable."))
        }
    }
}
