package com.avito.android.test.report

interface TestCaseAssertion {
    fun assertion(assertionMessage: String, action: () -> Unit)
}
