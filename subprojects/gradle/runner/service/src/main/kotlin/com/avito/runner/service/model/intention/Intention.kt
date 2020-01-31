package com.avito.runner.service.model.intention

data class Intention(
    val state: State,
    val action: Action
) {
    override fun toString(): String = "Intention with action: $action"
}
