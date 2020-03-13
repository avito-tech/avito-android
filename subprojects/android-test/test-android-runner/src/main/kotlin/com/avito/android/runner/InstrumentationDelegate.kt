package com.avito.android.runner

import android.os.Bundle

abstract class InstrumentationDelegate {

    open fun beforeOnCreate(arguments: Bundle) {

    }

    open fun afterOnCreate() {

    }

    /**
     * Called from instrumentation thread
     */
    open fun beforeOnStart() {

    }

    /**
     * Called from instrumentation thread
     */
    open fun afterOnStart() {

    }

    open fun onException(obj: Any?, e: Throwable): Boolean {
        return false
    }

    /**
     * Called from instrumentation thread
     */
    open fun onFinish(resultCode: Int, results: Bundle) {

    }
}
