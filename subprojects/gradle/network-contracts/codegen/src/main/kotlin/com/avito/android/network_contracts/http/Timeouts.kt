package com.avito.android.network_contracts.http

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
