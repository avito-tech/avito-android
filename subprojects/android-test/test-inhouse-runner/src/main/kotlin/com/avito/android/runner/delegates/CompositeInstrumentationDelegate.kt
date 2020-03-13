package com.avito.android.runner.delegates

import android.os.Bundle
import com.avito.android.runner.InstrumentationDelegate

class CompositeInstrumentationDelegate(
    private val delegates: List<InstrumentationDelegate>
) : InstrumentationDelegate() {
    override fun beforeOnCreate(arguments: Bundle) {
        delegates.forEach { it.beforeOnCreate(arguments) }
    }

    override fun afterOnCreate() {
        delegates.forEach { it.afterOnCreate() }
    }

    override fun beforeOnStart() {
        delegates.forEach { it.beforeOnStart() }
    }

    override fun afterOnStart() {
        delegates.forEach { it.afterOnStart() }
    }

    override fun onException(obj: Any?, e: Throwable): Boolean {
        return delegates.any { it.onException(obj, e) }
    }

    override fun onFinish(resultCode: Int, results: Bundle) {
        delegates.forEach { it.onFinish(resultCode, results) }
    }
}