package com.avito.runner.service.model.intention

public data class Intention(
    val state: State,
    val action: InstrumentationTestRunAction
) {
    override fun toString(): String = "Intention: $action, state: $state"

    public companion object
}
