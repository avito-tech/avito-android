package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.UITestConfig
import com.avito.android.test.app.core.screenRule
import com.avito.android.test.interceptor.HumanReadableActionInterceptor
import com.avito.android.test.interceptor.HumanReadableAssertionInterceptor
import com.avito.android.ui.VisibilityActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InterceptorTest {

    @get:Rule
    val rule = screenRule<VisibilityActivity>(true)

    @Test
    fun actionInterceptor_intercepts_clickAction() {
        var intercepted = ""

        UITestConfig.actionInterceptors += HumanReadableActionInterceptor { intercepted = it }
        Screen.visibility.label.click()

        assertEquals(
            "single click on clickable element on enabled element on AppCompatTextView(id=text;text=Test)",
            intercepted
        )
    }

    @Test
    fun assertionInterceptor_intercepts_visibleCheck() {
        var intercepted = ""

        UITestConfig.assertionInterceptors += HumanReadableAssertionInterceptor { intercepted = it }

        Screen.visibility.label.checks.isVisible()

        assertEquals(
            "Check view has effective visibility <VISIBLE> on AppCompatTextView(id=text;text=Test)",
            intercepted
        )
    }
}
