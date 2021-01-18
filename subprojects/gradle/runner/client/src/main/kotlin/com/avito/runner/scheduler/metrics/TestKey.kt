package com.avito.runner.scheduler.metrics

import com.avito.runner.service.model.TestCase

internal data class TestKey(
    val test: TestCase,
    val executionNumber: Int
)
