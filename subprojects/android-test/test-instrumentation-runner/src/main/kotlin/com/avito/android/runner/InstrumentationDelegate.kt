package com.avito.android.runner

import com.avito.logger.LoggerFactory

class InstrumentationDelegate(
    errorsReporter: ErrorsReporter,
    loggerFactory: LoggerFactory
) {

    private val systemDialogsManager = SystemDialogsManager(
        errorsReporter = errorsReporter,
        loggerFactory = loggerFactory
    )

    fun beforeOnStart() {
        systemDialogsManager.closeSystemDialogs()
    }
}
