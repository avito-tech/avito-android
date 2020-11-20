package com.avito.android.runner

interface ErrorsReporter {
    fun reportError(error: Throwable)
}
