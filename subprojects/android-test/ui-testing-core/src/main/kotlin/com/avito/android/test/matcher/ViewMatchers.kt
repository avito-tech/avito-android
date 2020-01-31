package com.avito.android.test.matcher

import org.hamcrest.Matchers.`is`

internal object ViewMatchers {

    /**
     * Matches if any possible kind of checkable state is true
     * If it is not working in your case consider modifying matcher
     */
    fun isChecked() = UniversalCheckedMatcher(`is`(true))

    /**
     * Matches if any possible kind of checkable state is false
     * If it is not working in your case consider modifying matcher
     */
    fun isNotChecked() = UniversalCheckedMatcher(`is`(false))

    /**
     * Matches if view has specified alpha value
     */
    fun hasAlpha(alpha: Float) = AlphaMatcher(`is`(alpha))

    /**
     * Matches if view is focusable in touch mode
     */
    fun isFocusableInTouchMode() = FocusableInTouchModeMatcher()
}
