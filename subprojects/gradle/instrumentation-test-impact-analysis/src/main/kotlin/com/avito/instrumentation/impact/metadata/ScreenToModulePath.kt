package com.avito.instrumentation.impact.metadata

import org.gradle.util.Path

data class ScreenToModulePath(
    val screenClass: String,
    val modulePath: Path
)
