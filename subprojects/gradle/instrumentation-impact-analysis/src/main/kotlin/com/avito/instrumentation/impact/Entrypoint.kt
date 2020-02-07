package com.avito.instrumentation.impact

import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer

fun TaskContainer.analyzeTestImpactTask() = typedNamed<AnalyzeTestImpactTask>(TASK_ANALYZE_TEST_IMPACT_ANALYSIS)
