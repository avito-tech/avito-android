package com.avito.android.runner

import android.util.Log

class LogErrorsReporter(
    private val tag: String = "ErrorsReporter"
) : ErrorsReporter {

    override fun reportError(error: Throwable) {
        Log.e(tag, "ERROR", error)
    }
}