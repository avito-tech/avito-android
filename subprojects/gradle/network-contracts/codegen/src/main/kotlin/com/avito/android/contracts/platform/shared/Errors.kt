package com.avito.android.contracts.platform.shared

import org.gradle.api.GradleException

internal fun throwGradleError(message: String, error: Throwable? = null): Nothing =
    throw GradleException(message, error)
