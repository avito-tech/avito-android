package com.avito.runner.service.model.intention

data class Intention(
    val state: State,
    val action: InstrumentationTestRunAction
) {
    override fun toString(): String = "Intention: $action, state: $state"

    companion object
}
