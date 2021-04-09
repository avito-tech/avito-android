@file:Suppress("MatchingDeclarationName")
package com.avito.android.test.report

import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.report.model.DataSet
import com.avito.android.util.ImitateFlagProvider

@PublishedApi
internal object TestExecutionState {

    val onlyImitateProvider: ImitateFlagProvider
        get() = InstrumentationRegistry.getInstrumentation() as ImitateFlagProvider

    val reportInstance: Report
        get() = (InstrumentationRegistry.getInstrumentation() as ReportProvider).report
}

inline fun step(
    description: String,
    action: TestCaseAssertion.() -> Unit = {}
) {
    step(
        name = description,
        report = TestExecutionState.reportInstance,
        onlyImitate = TestExecutionState.onlyImitateProvider.isImitate,
        action = action
    )
}

@PublishedApi
internal inline fun step(
    name: String,
    report: Report,
    onlyImitate: Boolean,
    action: TestCaseAssertion.() -> Unit
) {
    internalStep(
        step = Step(
            stepName = name,
            report = report
        ),
        onlyImitate = onlyImitate,
        block = action
    )
}

inline fun precondition(
    description: String,
    action: TestCaseAssertion.() -> Unit
) {
    precondition(
        name = description,
        report = TestExecutionState.reportInstance,
        onlyImitate = TestExecutionState.onlyImitateProvider.isImitate,
        action = action
    )
}

@PublishedApi
internal inline fun precondition(
    name: String,
    report: Report,
    onlyImitate: Boolean,
    action: TestCaseAssertion.() -> Unit
) {
    internalStep(
        step = Precondition(
            stepName = name,
            report = report
        ),
        onlyImitate = onlyImitate,
        block = action
    )
}

inline fun <T : DataSet> dataSet(
    value: T,
    action: (T) -> Unit
) {
    TestExecutionState.reportInstance.updateTestCase {
        dataSet = value
        // todo почему -1 это вообще валидное значение? попробовать использовать unsigned тип данных
        require(testMetadata.dataSetNumber != null && testMetadata.dataSetNumber != -1) {
            "Please specify @DataSetNumber(Int) for test ${testMetadata.testName}"
        }
    }
    action(value)
}

@PublishedApi
internal inline fun <T> internalStep(
    step: BaseStep,
    onlyImitate: Boolean,
    block: TestCaseAssertion.() -> T
) {
    with(step) {
        // must be out of try because if we fail while starting we don't want to execute stepFailed() stepFinished()
        stepStart()
        try {
            if (!onlyImitate) {
                this.block()
            }
            stepPassed()
        } catch (t: Throwable) {
            stepFailed(t)
        } finally {
            stepFinished()
        }
    }
}
