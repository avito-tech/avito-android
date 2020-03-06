package com.avito.android.ui.test

import com.avito.android.rule.ToastRule
import com.avito.android.ui.ToastActivity
import org.junit.Rule
import org.junit.Test

class ToastTest {

    @get:Rule
    val rule = screenRule<ToastActivity>()

    @get:Rule
    val toast = ToastRule()

    @Test
    fun toastIsDisplayedWithText() {
        rule.launchActivity(null)

        toast.checks.toastDisplayedWithText("I'am a toast!")
    }
}
