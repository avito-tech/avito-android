package com.avito.android.runner

import android.os.Bundle

internal class DelegatesRegistry(
    private val delegates: List<InstrumentationTestRunnerDelegate>
) : InstrumentationTestRunnerDelegate() {

    override fun beforeOnCreate(arguments: Bundle) {
        delegates.forEach { it.beforeOnCreate(arguments) }
    }

    override fun afterOnCreate(arguments: Bundle) {
        delegates.forEach { it.afterOnCreate(arguments) }
    }

    override fun beforeOnStart() {
        delegates.forEach { it.beforeOnStart() }
    }
}
