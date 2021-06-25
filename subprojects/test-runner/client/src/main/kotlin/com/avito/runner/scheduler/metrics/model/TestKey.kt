package com.avito.runner.scheduler.metrics.model

import com.avito.test.model.TestCase

internal data class TestKey(
    val test: TestCase,
    val executionNumber: Int
) {

    companion object
}
