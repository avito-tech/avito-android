package com.avito.android.rule

import android.view.View
import androidx.annotation.StringRes
import org.hamcrest.Matcher

interface ToastChecks {

    fun toastDisplayedWithText(text: String)

    fun toastDisplayedWithText(@StringRes resId: Int)

    @Deprecated(
        "Use toastDisplayedWithView for custom views " +
            "or toastDisplayedWithText(String|StrintRes) for simple toasts"
    )
    fun toastDisplayedWithText(matcher: Matcher<View>)

    /**
     * Toasts with custom views
     */
    fun toastDisplayedWithView(matcher: Matcher<View>)

    fun toastNotDisplayed()
}
