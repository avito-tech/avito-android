package com.avito.android.ui.test.retry

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.avito.android.screen.Screen
import com.avito.android.test.page_object.PageObject
import com.avito.android.test.page_object.ViewElement
import com.avito.android.ui.R

class RetryScreen : PageObject(), Screen {
    override val rootId: Int = R.id.activity_retry

    val button: ViewElement = element(withId(R.id.button))
    val buttonClickIndicator: ViewElement = element(
        withId(R.id.button_click_indicator)
    )
}
