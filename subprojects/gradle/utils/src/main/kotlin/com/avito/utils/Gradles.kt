package com.avito.utils

import org.gradle.api.invocation.Gradle

fun Gradle.onBuildFailed(block: () -> Unit) {
    buildFinished { buildResult ->
        if (buildResult.failure != null && buildResult.action == ACTION_BUILD) {
            block()
        }
    }
}

private const val ACTION_BUILD = "Build"
