package com.avito.runner.service.model.intention

data class IntentionResult(
    val intention: Intention,
    val actionResult: InstrumentationTestRunActionResult
)
