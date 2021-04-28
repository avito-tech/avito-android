package com.avito.android.test.report.impl

import com.avito.android.test.report.InternalReport
import com.avito.android.test.report.ReportState
import com.avito.android.test.report.StepException
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

internal class SynchronizationReport(
    private val report: InternalReport
) : InternalReport {

    private val instrumentationThread = AtomicReference<Thread?>()
    private val transitionStateLock = ReentrantLock(true)
    private val mutationsInProgressCount = AtomicInteger(0)

    override val currentState: ReportState
        get() = report.currentState

    override val isFirstStep: Boolean
        get() {
            checkInstrumentationThread()
            return report.isFirstStep
        }

    override fun initTestCase(testMetadata: TestMetadata) {
        require(instrumentationThread.compareAndSet(null, Thread.currentThread())) {
            "initTestCase must be called only once"
        }
        transitionState {
            report.initTestCase(testMetadata)
        }
    }

    override fun startTestCase() {
        checkInstrumentationThread()
        transitionState {
            report.startTestCase()
        }
    }

    override fun finishTestCase() {
        transitionState {
            report.finishTestCase()
        }
    }

    override fun failedTestCase(exception: Throwable) {
        checkInstrumentationThread()
        transitionState {
            report.failedTestCase(exception)
        }
    }

    override fun unexpectedFailedTestCase(exception: Throwable) {
        transitionState {
            report.unexpectedFailedTestCase(exception)
        }
    }

    override fun setDataSet(value: DataSet) {
        checkInstrumentationThread()
        mutateState(
            mutation = {
                report.setDataSet(value)
            }
        )
    }

    override fun startPrecondition(step: StepResult) {
        checkInstrumentationThread()
        mutateState(
            mutation = {
                report.startPrecondition(step)
            }
        )
    }

    override fun stopPrecondition() {
        checkInstrumentationThread()
        mutateState(
            mutation = {
                report.stopPrecondition()
            }
        )
    }

    override fun preconditionFailed(exception: StepException) {
        checkInstrumentationThread()
        mutateState(
            mutation = {
                report.preconditionFailed(exception)
            }
        )
    }

    override fun createPreconditionModel(stepName: String): StepResult {
        checkInstrumentationThread()
        return report.createPreconditionModel(stepName)
    }

    override fun startStep(step: StepResult) {
        checkInstrumentationThread()
        mutateState(
            mutation = {
                report.startStep(step)
            }
        )
    }

    override fun stopStep() {
        checkInstrumentationThread()
        mutateState(
            mutation = {
                report.stopStep()
            }
        )
    }

    override fun stepFailed(exception: StepException) {
        checkInstrumentationThread()
        mutateState(
            mutation = {
                report.stepFailed(exception)
            }
        )
    }

    override fun createStepModel(stepName: String): StepResult {
        checkInstrumentationThread()
        return report.createStepModel(stepName)
    }

    override fun addHtml(label: String, content: String, wrapHtml: Boolean) {
        mutateState(
            mutation = {
                report.addHtml(label, content, wrapHtml)
            }
        )
    }

    override fun addText(label: String, text: String) {
        mutateState(
            mutation = {
                report.addText(label, text)
            }
        )
    }

    override fun addComment(comment: String) {
        mutateState(
            mutation = {
                report.addComment(comment)
            }
        )
    }

    override fun addScreenshot(label: String) {
        mutateState(
            mutation = {
                report.addScreenshot(label)
            }
        )
    }

    override fun addAssertion(label: String) {
        mutateState(
            mutation = {
                report.addAssertion(label)
            }
        )
    }

    private inline fun mutateState(
        mutation: () -> Unit
    ) {
        var isIncremented = false
        try {
            try {
                transitionStateLock.lock()
                mutationsInProgressCount.incrementAndGet()
                isIncremented = true
            } finally {
                transitionStateLock.unlock()
            }
            mutation()
        } finally {
            if (isIncremented) {
                mutationsInProgressCount.decrementAndGet()
            }
        }
    }

    @Suppress("LoopWithTooManyJumpStatements")
    private inline fun transitionState(action: () -> Unit) {
        while (true) {
            try {
                transitionStateLock.lock()
                val value = mutationsInProgressCount.get()
                check(value >= 0) {
                    "Failed transitionState. mutationsInProgressCount was $value but must be >= 0"
                }
                if (value == 0) {
                    action()
                    break
                } else {
                    continue
                }
            } finally {
                transitionStateLock.unlock()
            }
        }
    }

    private fun checkInstrumentationThread() {
        val instrumentationThread = instrumentationThread.get()
        checkNotNull(instrumentationThread) {
            "Instrumentation thread must be initialized. Look initTestCase()"
        }
        check(instrumentationThread == Thread.currentThread()) {
            "Must be called from instrumentation thread $instrumentationThread"
        }
    }
}
