package com.avito.android.test.interceptor

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import org.hamcrest.Matcher

/**
 * Register your interceptors in [com.avito.android.test.UITestConfig]
 * intercept() will be called before any action during tests
 */
interface ActionInterceptor {

    fun intercept(action: ViewAction, description: String, view: View)

    class Proxy(
        private val source: ViewAction,
        private val interceptors: List<ActionInterceptor>
    ) : ViewAction {

        override fun getDescription(): String = source.description

        override fun getConstraints(): Matcher<View> = source.constraints

        override fun perform(uiController: UiController, view: View) {
            interceptors.forEach { it.intercept(source, description, view) }
            source.perform(uiController, view)
        }
    }
}

/**
 * Register your interceptors in [com.avito.android.test.UITestConfig]
 * intercept() will be called before any action during tests
 */
interface AssertionInterceptor {

    fun intercept(
        assertion: ViewAssertion,
        view: View?,
        noViewFoundException: NoMatchingViewException?
    )

    class Proxy(
        private val source: ViewAssertion,
        private val interceptors: List<AssertionInterceptor>
    ) : ViewAssertion {

        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            interceptors.forEach { it.intercept(source, view, noViewFoundException) }
            source.check(view, noViewFoundException)
        }
    }
}
