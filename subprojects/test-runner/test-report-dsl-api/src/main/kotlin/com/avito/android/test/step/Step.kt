package com.avito.android.test.step

import com.avito.android.test.report.TestCaseAssertion

interface Step : TestCaseAssertion {
    fun stepStart()
    fun stepPassed()
    fun stepFailed(exception: Throwable)
    fun stepFinished()
}
