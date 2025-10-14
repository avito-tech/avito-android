package com.avito.android.contracts.platform.analytics

import com.avito.utils.gradle.BuildEnvironment

public enum class ActionEnvironment(public val value: String) {
    LOCAL("local"),
    IDE("ide"),
    CI("ci"),
}

public fun BuildEnvironment.toActionEnvironment(): ActionEnvironment? {
    return when (this) {
        is BuildEnvironment.CI -> ActionEnvironment.CI
        is BuildEnvironment.IDE -> ActionEnvironment.IDE
        is BuildEnvironment.Local -> ActionEnvironment.LOCAL
        else -> null
    }
}
