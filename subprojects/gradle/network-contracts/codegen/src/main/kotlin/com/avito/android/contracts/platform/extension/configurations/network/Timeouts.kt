package com.avito.android.contracts.platform.extension.configurations.network

import java.io.Serializable
import java.time.Duration

public data class Timeouts(
    val readTimeout: Duration = Duration.ofSeconds(10),
    val writeTimeout: Duration = Duration.ofSeconds(10),
    val connectTimeout: Duration = Duration.ofSeconds(10),
) : Serializable {

    public companion object {

        public fun of(timeout: Duration): Timeouts {
            return Timeouts(timeout, timeout, timeout)
        }
    }
}
