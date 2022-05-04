package com.avito.emcee.client

import kotlin.time.ExperimentalTime

public interface EmceeTestClient {

    @ExperimentalTime
    public fun execute(config: EmceeTestClientConfig)
}
