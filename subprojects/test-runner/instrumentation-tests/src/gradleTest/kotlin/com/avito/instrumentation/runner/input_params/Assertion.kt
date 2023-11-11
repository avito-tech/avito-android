package com.avito.instrumentation.runner.input_params

import com.avito.runner.config.RunnerInputParams

data class Assertion(
    val name: String,
    val assert: (RunnerInputParams) -> Unit
)
