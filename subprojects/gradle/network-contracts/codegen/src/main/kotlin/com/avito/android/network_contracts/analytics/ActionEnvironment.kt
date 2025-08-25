package com.avito.android.network_contracts.analytics

import com.avito.utils.gradle.BuildEnvironment

internal enum class ActionEnvironment(val value: String) {
    LOCAL("local"),
    IDE("ide"),
    CI("ci"),
}

internal fun BuildEnvironment.toActionEnvironment(): ActionEnvironment? {
    return when (this) {
        is BuildEnvironment.CI -> ActionEnvironment.CI
        is BuildEnvironment.IDE -> ActionEnvironment.IDE
        is BuildEnvironment.Local -> ActionEnvironment.LOCAL
        else -> null
    }
}
