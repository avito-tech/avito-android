package com.avito.android.test.step

import com.avito.android.test.report.TestCaseAssertion

public interface Step : TestCaseAssertion {
    public fun stepStart()
    public fun stepPassed()
    public fun stepFailed(exception: Throwable)
    public fun stepFinished()
}
