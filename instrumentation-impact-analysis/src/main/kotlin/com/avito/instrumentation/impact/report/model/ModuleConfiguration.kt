package com.avito.instrumentation.impact.report.model

internal data class ModuleConfiguration(
    val dependencies: List<String>,
    val changedFiles: List<String>,
    val isModified: Boolean,
    val isPrimaryModified: Boolean,
    val modifiedReason: String?
)

