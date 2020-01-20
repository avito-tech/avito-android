package com.avito.android.test.interceptor

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.describe
import com.avito.android.test.util.describe

class HumanReadableActionInterceptor(private val consumer: (String) -> Unit) :
    ActionInterceptor {

    override fun intercept(action: ViewAction, description: String, view: View) {
        consumer.invoke("$description on ${view.describe()}")
    }
}

class HumanReadableAssertionInterceptor(private val consumer: (String) -> Unit) :
    AssertionInterceptor {

    override fun intercept(
        assertion: ViewAssertion,
        view: View?,
        noViewFoundException: NoMatchingViewException?
    ) {
        consumer.invoke("${assertion.describe()} on ${view.describe()}")
    }
}
