package com.avito.instrumentation.internal.suite.model

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.report.model.TestStaticData

public data class TestWithTarget(
    val test: TestStaticData,
    val target: TargetConfiguration.Data
)
