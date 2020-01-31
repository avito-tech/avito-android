package com.avito.android.test.report

/**
 * Just invokes the action
 */
class TestCaseAssertion(
    @PublishedApi internal var assertionMessage: String? = null,
    @PublishedApi internal var beforeAssertion: (String) -> Unit
) {

    inline fun assertion(assertionMessage: String, action: () -> Unit) {
        this.assertionMessage = assertionMessage
        beforeAssertion.invoke(assertionMessage)
        action.invoke()
    }
}
