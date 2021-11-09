package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.DistantViewOnScrollActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollViewScrollToEndTest {

    @get:Rule
    val rule = screenRule<DistantViewOnScrollActivity>(launchActivity = true)

    @Test
    fun isVisible_viewIsNotDisplayed() {
        Screen.distantViewOnScroll.view.checks.isNotDisplayed()
    }
}
