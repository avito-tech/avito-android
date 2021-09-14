package com.avito.android.ui.test

import androidx.test.espresso.matcher.ViewMatchers
import com.avito.android.rule.ToastRule
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.R
import com.avito.android.ui.ToastActivity
import org.junit.Rule
import org.junit.Test
import ru.avito.util.assertThrows

class ToastTest {

    @get:Rule
    val rule = screenRule<ToastActivity>()

    @get:Rule
    val toast = ToastRule()

    @Test
    fun toastIsDisplayedWithText_String__success__simple_toast() {
        rule.launchActivity(
            ToastActivity.intent(showSimpleToast = true)
        )

        toast.checks.toastDisplayedWithText("Simple toast")
    }

    @Test
    fun toastIsDisplayedWithText_res__success__simple_toast() {
        rule.launchActivity(
            ToastActivity.intent(showSimpleToast = true)
        )

        toast.checks.toastDisplayedWithText(R.string.simple_toast_text)
    }

    @Test
    fun toastIsDisplayedWithText__success__custom_toast() {
        rule.launchActivity(
            ToastActivity.intent(showCustomToast = true)
        )

        toast.checks.toastDisplayedWithView(ViewMatchers.withText("Custom toast"))
    }

    @Test
    fun toastIsDisplayedWithText__fail__no_toast() {
        rule.launchActivity(
            ToastActivity.intent(showSimpleToast = false)
        )
        assertThrows<AssertionError> {
            toast.checks.toastDisplayedWithText("Simple toast")
        }
    }
}
