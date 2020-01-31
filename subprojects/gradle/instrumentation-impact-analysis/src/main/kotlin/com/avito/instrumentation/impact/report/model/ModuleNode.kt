package com.avito.instrumentation.impact.report.model

internal data class ModuleNode(
    val name: String,
    val changedFiles: List<String>,

    val implementationConfiguration: ModuleConfiguration,
    val testConfiguration: ModuleConfiguration,
    val androidTestConfiguration: ModuleConfiguration,
    val lintConfiguration: ModuleConfiguration
)
