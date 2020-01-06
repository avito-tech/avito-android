package com.avito.impact.configuration.sets

import com.android.build.gradle.api.AndroidSourceSet

internal fun AndroidSourceSet.isImplementation() = !(isTest() or isAndroidTest())
internal fun AndroidSourceSet.isTest(): Boolean = name.startsWith("test")
internal fun AndroidSourceSet.isAndroidTest(): Boolean = name.startsWith("androidTest")
