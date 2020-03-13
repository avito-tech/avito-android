package com.avito.android.test.report

import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.util.ImitateFlagProvider

@PublishedApi
internal val onlyImitateProvider: ImitateFlagProvider
    get() = InstrumentationRegistry.getInstrumentation() as ImitateFlagProvider

@PublishedApi
@Deprecated("")
internal val reportInstance: Report
    get() = (InstrumentationRegistry.getInstrumentation() as ReportProvider).report

inline fun step(
    description: String,
    report: Report = reportInstance,
    onlyImitate: Boolean = onlyImitateProvider.isImitate,
    action: TestCaseAssertion.() -> Unit = {}
) {
    internalStep(
        isPrecondition = false,
        action = description,
        onlyImitate = onlyImitate,
        report = report,
        block = action
    )
}

inline fun precondition(
    description: String,
    report: Report = reportInstance,
    onlyImitate: Boolean = onlyImitateProvider.isImitate,
    action: TestCaseAssertion.() -> Unit
) {
    internalStep(
        isPrecondition = true,
        action = description,
        onlyImitate = onlyImitate,
        report = report,
        block = action
    )
}

inline fun <T : DataSet> dataSet(
    value: T,
    report: Report = reportInstance,
    action: (T) -> Unit
) {
    report.updateTestCase {
        dataSet = value
        //todo почему -1 это вообще валидное значение? попробовать использовать unsigned тип данных
        require(testMetadata.dataSetNumber != null && testMetadata.dataSetNumber != -1) {
            "Please specify @DataSetNumber(Int) for test ${testMetadata.className}.${testMetadata.methodName}"
        }
    }
    action(value)
}

inline fun <T> internalStep(
    isPrecondition: Boolean,
    action: String,
    onlyImitate: Boolean,
    report: Report,
    block: TestCaseAssertion.() -> T
) {
    with(InternalStep(isPrecondition, report)) {
        stepStart(action)
        val assertion = TestCaseAssertion(beforeAssertion = { beforeAssertion(it) })
        try {
            if (!onlyImitate) {
                block.invoke(assertion)
            }
            stepPassed()
        } catch (t: Throwable) {
            val stepException = StepException(isPrecondition, action, assertion.assertionMessage, t)
            stepFailed(stepException)
            throw stepException
        } finally {
            stepFinished()
        }
    }
}

class StepException(val isPrecondition: Boolean, val action: String, val assertion: String?, cause: Throwable?) :
    ReporterException(message = title(isPrecondition) + "\n" + data(isPrecondition, action, assertion), cause = cause) {

    companion object {
        private fun slug(isPrecondition: Boolean) = if (isPrecondition) "precondition" else "шаг"

        fun title(isPrecondition: Boolean) = "Не удалось выполнить ${slug(isPrecondition)}"

        fun data(isPrecondition: Boolean, action: String, assertion: String?): String {
            return "${slug(isPrecondition).capitalize()}:\n${action.prependIndent()}"
                .let {
                    if (assertion != null)
                        "$it\nПроверка:\n${assertion.prependIndent()}"
                    else it
                }
        }
    }
}

class InternalStep(private val isPrecondition: Boolean, private val report: Report) {

    private val slug = if (isPrecondition) "precondition'ом" else "шагом"
    private val slug2 = if (isPrecondition) "precondition'a" else "шага"

    /**
     * It's completely legal to do multiple assertions in single step, but we need only one screenshot,
     * because system state must not be changed anymore in this particular step
     */
    private var screenshotBeforeAssertionDone = false

    fun stepStart(name: String) {
        val newStep = StepResult().apply { title = name }
        if (isPrecondition) {
            report.startPrecondition(newStep)
        } else {
            report.startStep(newStep)
        }

        if (!report.isFirstStepOrPrecondition) {
            report.makeScreenshot("Screenshot перед $slug")
        }
    }

    /**
     * We need screenshot in the moment right before first assertion.
     * Assume that we don't change system state anymore for this step
     */
    fun beforeAssertion(assertionText: String) {
        report.addAssertion(assertionText)

        if (!screenshotBeforeAssertionDone) {
            report.makeScreenshot("Screenshot перед проверкой: $assertionText")
            screenshotBeforeAssertionDone = true
        }
    }

    /**
     * For first precondition we make screenshot after step, not before, because app may not be started at this moment
     */
    fun stepPassed() {
        if (report.isFirstStepOrPrecondition) {
            report.makeScreenshot("Screenshot после $slug2")
        }
    }

    fun stepFailed(exception: Throwable) {
        report.registerIncident(
            exception,
            report.makeScreenshot("Screenshot после падения $slug2")
        )
    }

    /**
     * will be called no matter if step was successful or failed
     */
    fun stepFinished() {
        if (isPrecondition) {
            report.stopPrecondition()
        } else {
            report.stopStep()
        }
    }
}
