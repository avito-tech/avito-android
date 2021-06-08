package com.avito.runner.scheduler.runner.model

import com.avito.report.model.TestStaticData
import com.avito.runner.config.TargetConfigurationData

public data class TestWithTarget(
    val test: TestStaticData,
    val target: TargetConfigurationData
)
