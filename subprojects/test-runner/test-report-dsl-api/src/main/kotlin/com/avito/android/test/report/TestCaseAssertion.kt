package com.avito.android.test.report

public interface TestCaseAssertion {
    public fun assertion(assertionMessage: String, action: () -> Unit)
}
