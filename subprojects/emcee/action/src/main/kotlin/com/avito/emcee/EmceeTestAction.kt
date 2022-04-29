package com.avito.emcee

import kotlin.time.ExperimentalTime

public interface EmceeTestAction {

    @ExperimentalTime
    public fun execute(config: EmceeTestActionConfig)
}
