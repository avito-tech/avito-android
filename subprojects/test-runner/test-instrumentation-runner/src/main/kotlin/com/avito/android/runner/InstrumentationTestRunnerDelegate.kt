package com.avito.android.runner

import android.os.Bundle

public abstract class InstrumentationTestRunnerDelegate {

    public open fun beforeOnStart() {
        // empty
    }

    public open fun beforeOnCreate(arguments: Bundle) {
        // empty
    }

    public open fun afterOnCreate(arguments: Bundle) {
        // empty
    }

    public open fun beforeFinish(resultCode: Int, results: Bundle?) {
        // empty
    }

    public open fun afterFinish(resultCode: Int, results: Bundle?) {
        // empty
    }
}
