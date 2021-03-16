package com.avito.android.runner

import android.os.Bundle

abstract class InstrumentationTestRunnerDelegate {

    open fun beforeOnStart() {
        // empty
    }

    open fun beforeOnCreate(arguments: Bundle) {
        // empty
    }

    open fun afterOnCreate(arguments: Bundle) {
        // empty
    }
}
