package com.avito.android.runner

class InstrumentationDelegate(
    errorsReporter: ErrorsReporter
) {
    private val systemDialogsManager =
        SystemDialogsManager(
            errorsReporter = errorsReporter
        )

    fun beforeOnStart() {
        systemDialogsManager.closeSystemDialogs()
    }
}
