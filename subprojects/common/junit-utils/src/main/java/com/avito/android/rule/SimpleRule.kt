package com.avito.android.rule

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

abstract class SimpleRule : TestRule {

    final override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                validatePreconditions()
                before()
                val executionError = execute()
                val finalizeError = cleanup()
                failIfError(executionError, finalizeError)
            }

            private fun execute() = executeSafely {
                base.evaluate()
            }

            private fun cleanup() = executeSafely {
                after()
            }

            private fun executeSafely(block: () -> Unit): Throwable? {
                return try {
                    block()
                    null
                } catch (error: Throwable) {
                    error
                }
            }

            private fun failIfError(executionError: Throwable?, finalizeError: Throwable?) {
                if (finalizeError != null) {
                    if (executionError != null) {
                        finalizeError.initCause(executionError)
                    }
                    throw finalizeError
                } else if (executionError != null) {
                    throw executionError
                }
            }
        }

    // TODO: make an abstract after implementing in clients MBS-10013
    protected open fun validatePreconditions() {}

    protected open fun before() {}

    protected open fun after() {}
}
