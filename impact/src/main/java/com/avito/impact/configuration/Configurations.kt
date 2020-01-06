package com.avito.impact.configuration

import org.gradle.api.artifacts.Configuration

internal fun Configuration.isImplementation() = !(isTest() or isAndroidTest() or isLint())
internal fun Configuration.isTest(): Boolean = name.startsWith("test")
internal fun Configuration.isAndroidTest(): Boolean = name.startsWith("androidTest")
internal fun Configuration.isLint(): Boolean = name.startsWith("lint")
