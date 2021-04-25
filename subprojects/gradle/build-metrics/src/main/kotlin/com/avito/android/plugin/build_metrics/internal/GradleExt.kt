package com.avito.android.plugin.build_metrics.internal

import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.internal.operations.BuildOperationListenerManager

internal fun Gradle.buildOperationListenerManager(): BuildOperationListenerManager =
    (this as GradleInternal).services[BuildOperationListenerManager::class.java]
