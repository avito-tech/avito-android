package com.avito.runner.scheduler.metrics.model

import com.avito.runner.service.model.TestCase

internal data class TestKey(
    val test: TestCase,
    val executionNumber: Int
) {

    companion object
}
