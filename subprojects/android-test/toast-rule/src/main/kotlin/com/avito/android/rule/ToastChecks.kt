package com.avito.android.rule

import android.view.View
import androidx.annotation.IdRes
import org.hamcrest.Matcher

interface ToastChecks {
    fun toastDisplayedWithText(text: String)
    fun toastDisplayedWithText(@IdRes resId: Int)
    fun toastDisplayedWithText(matcher: Matcher<View>)
    fun toastNotDisplayed()
}
