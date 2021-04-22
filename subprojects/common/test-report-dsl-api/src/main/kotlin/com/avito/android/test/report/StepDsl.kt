@file:Suppress("MatchingDeclarationName")

package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.step.Step
import com.avito.android.test.step.StepDslDelegate

object StepDslProvider {

    private var stepDslDelegate: StepDslDelegate? = null

    @Synchronized
    fun initialize(executor: StepDslDelegate) {
        require(stepDslDelegate == null) {
            "Step executor already initialized"
        }
        stepDslDelegate = executor
    }

    @Synchronized
    @PublishedApi
    internal fun getDelegate(): StepDslDelegate {
        return requireNotNull(stepDslDelegate) {
            "StepExecutor must be initialized. Best place is your Instrumentation before any test execution"
        }
    }
}

/**
 * The reason that we use inline is reduced amount of generated code and
 * this reduces total amount of methods and helps API lower then 19 fits single DEX
 *
 * If you want to delete inline you must check impact on APK and build time
 */
inline fun step(
    description: String,
    action: TestCaseAssertion.() -> Unit = {}
) {
    internalStep(
        step = StepDslProvider.getDelegate().createStep(description),
        action = action
    )
}

inline fun precondition(
    description: String,
    action: TestCaseAssertion.() -> Unit = {}
) {
    internalStep(
        step = StepDslProvider.getDelegate().createPrecondition(description),
        action = action
    )
}

inline fun <T : DataSet> dataSet(
    value: T,
    action: (T) -> Unit
) {
    StepDslProvider.getDelegate().setDataSet(value)
    action(value)
}

@PublishedApi
internal inline fun internalStep(
    step: Step,
    action: TestCaseAssertion.() -> Unit
) {
    with(step) {
        // must be out of try because if we fail while starting we don't want to execute stepFailed() stepFinished()
        stepStart()
        try {
            action()
            stepPassed()
        } catch (t: Throwable) {
            stepFailed(t)
        } finally {
            stepFinished()
        }
    }
}
