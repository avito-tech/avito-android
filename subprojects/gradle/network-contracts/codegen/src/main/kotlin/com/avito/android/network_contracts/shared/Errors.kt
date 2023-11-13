package com.avito.android.network_contracts.shared

import org.gradle.api.GradleException

internal fun throwGradleError(message: String, error: Throwable? = null): Nothing =
    throw GradleException(message, error)
