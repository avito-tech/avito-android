package com.avito.android.runner

import com.avito.logger.LoggerFactory

class InstrumentationDelegate(loggerFactory: LoggerFactory) {

    private val systemDialogsManager = SystemDialogsManager(
        loggerFactory = loggerFactory
    )

    fun beforeOnStart() {
        systemDialogsManager.closeSystemDialogs()
    }
}
