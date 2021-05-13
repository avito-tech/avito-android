package com.avito.runner.service.model.intention

data class Intention(
    val state: State,
    val action: InstrumentationTestRunAction
) {
    override fun toString(): String = "Intention with action: $action, with state: $state"
}
