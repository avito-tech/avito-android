package com.avito.android.runner

open class UITestFrameworkException(
    override val message: String,
    override val cause: Throwable?
) : RuntimeException(message, cause)
