package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.step.Step
import com.avito.android.test.step.StepDslDelegate
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

internal object StepDslExtension : BeforeAllCallback, StepDslDelegate {

    lateinit var delegate: StepDslDelegate

    /**
     * JUnit Runner could execute tests in the same JVM process
     * It leads to problems with static
     *
     * So we initialized one time per JVM process, I don't find JUnit extension for that
     */
    private var initialized: Boolean = false

    override fun createStep(description: String): Step {
        return delegate.createStep(description)
    }

    override fun createPrecondition(description: String): Step {
        return delegate.createPrecondition(description)
    }

    override fun <T : DataSet> setDataSet(value: T) {
        delegate.setDataSet(value)
    }

    override fun beforeAll(context: ExtensionContext) {
        if (!initialized) {
            StepDslProvider.initialize(this)
            initialized = true
        }
    }
}
