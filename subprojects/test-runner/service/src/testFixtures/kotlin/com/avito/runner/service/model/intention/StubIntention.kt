package com.avito.runner.service.model.intention

internal fun Intention.Companion.createStubInstance(
    state: State,
    action: InstrumentationTestRunAction = InstrumentationTestRunAction.createStubInstance()
): Intention = Intention(
    state = state,
    action = action
)
