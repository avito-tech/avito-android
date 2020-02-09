package com.avito.impact.configuration.sets

import java.io.File

internal fun File.isImplementation() = !(isTest() or isAndroidTest())
internal fun File.isTest(): Boolean = name.endsWith("test") && !isAndroidTest()
internal fun File.isAndroidTest(): Boolean = name.endsWith("AndroidTest")
